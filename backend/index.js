const express = require('express');
const multer = require('multer');
const cors = require('cors');
const { S3Client } = require('@aws-sdk/client-s3');
const { Upload } = require('@aws-sdk/lib-storage');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');
const path = require('path');
const FormData = require('form-data');
const axios = require('axios');
const { createClient } = require('@supabase/supabase-js');
require('dotenv').config();

// Initialize Express app
const app = express();
app.use(cors());
app.use(express.json());

// Proxy logic to keep Supabase API key hidden
const REAL_SUPABASE_URL = process.env.SUPABASE_URL || ''; 
const REAL_SUPABASE_KEY = process.env.SUPABASE_KEY || '';

// Initialize Supabase Client
const supabase = (REAL_SUPABASE_URL && REAL_SUPABASE_KEY && REAL_SUPABASE_KEY !== "proxy-will-append-real-key") 
    ? createClient(REAL_SUPABASE_URL, REAL_SUPABASE_KEY) 
    : null;

// Cloudflare R2 Configuration (Optional)
let s3 = null;
if (process.env.R2_ACCOUNT_ID && process.env.R2_ACCESS_KEY_ID) {
    s3 = new S3Client({
        region: 'auto',
        endpoint: `https://${process.env.R2_ACCOUNT_ID}.r2.cloudflarestorage.com`,
        credentials: {
            accessKeyId: process.env.R2_ACCESS_KEY_ID,
            secretAccessKey: process.env.R2_SECRET_ACCESS_KEY,
        },
    });
}

// Configure multer for DISK storage to avoid OOM crashes on Render Free Tier
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        const uploadDir = path.join(__dirname, 'uploads');
        if (!fs.existsSync(uploadDir)) {
            fs.mkdirSync(uploadDir);
        }
        cb(null, uploadDir);
    },
    filename: function (req, file, cb) {
        const fileExtension = file.originalname.split('.').pop();
        cb(null, `${uuidv4()}.${fileExtension}`);
    }
});

const upload = multer({ 
    storage: storage,
    limits: { fileSize: 200 * 1024 * 1024 }, // 200MB limit
});

// --- API ROUTES ---

// Health Check Endpoint
app.get('/api/health', (req, res) => {
    res.json({ status: 'ok', message: 'Backend server is running properly with multiple storage fallbacks!' });
});

// Supabase Proxy
app.use(['/auth/v1', '/rest/v1'], async (req, res) => {
    try {
        if (!REAL_SUPABASE_URL || !REAL_SUPABASE_KEY || REAL_SUPABASE_KEY === "proxy-will-append-real-key" || REAL_SUPABASE_URL.includes("onrender.com")) {
            return res.status(500).json({ 
                error: 'server_error', 
                error_description: 'Backend Configuration Error: You must set REAL Supabase URL and SUPABASE_KEY in Environment Variables!'
            });
        }

        const baseUrl = REAL_SUPABASE_URL.endsWith('/') ? REAL_SUPABASE_URL.slice(0, -1) : REAL_SUPABASE_URL;
        const targetUrl = new URL(req.originalUrl, baseUrl).toString();
        
        const headers = new Headers();
        if (req.headers.authorization) {
            headers.append('Authorization', req.headers.authorization);
        } else {
            headers.append('Authorization', `Bearer ${REAL_SUPABASE_KEY}`);
        }
        
        headers.append('apikey', REAL_SUPABASE_KEY);
        if (req.headers['x-client-info']) headers.append('X-Client-Info', req.headers['x-client-info']);
        if (req.headers['content-type']) headers.append('Content-Type', req.headers['content-type']);
        if (req.headers.accept) headers.append('Accept', req.headers.accept);

        const fetchOptions = {
            method: req.method,
            headers: headers,
        };

        if (['POST', 'PUT', 'PATCH'].includes(req.method)) {
            if (req.headers['content-type'] && req.headers['content-type'].includes('application/json')) {
                fetchOptions.body = JSON.stringify(req.body);
            } else if (Object.keys(req.body || {}).length > 0) {
                 fetchOptions.body = JSON.stringify(req.body);
            }
        }

        const response = await fetch(targetUrl, fetchOptions);
        
        const contentType = response.headers.get('content-type');
        let data;
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
            return res.status(response.status).json(data);
        } else {
            data = await response.text();
            return res.status(response.status).send(data);
        }
    } catch (error) {
        console.error('Proxy Error:', error);
        return res.status(500).json({ error: 'Failed proxy request.', details: error.message });
    }
});

// Media Upload Endpoint (Video/Photo) with Storage Auto-Fallback
app.post('/api/upload', upload.single('media'), async (req, res) => {
    if (!req.file) {
        return res.status(400).json({ error: 'No media file provided or invalid format.' });
    }
    
    const filePath = req.file.path;
    const fileName = req.file.filename;
    let fileUrl = '';
    let storageMethod = '';

    try {
        // Strategy 1: Cloudflare R2 (If Configured)
        if (s3 && process.env.R2_BUCKET_NAME) {
            console.log('Attempting Cloudflare R2 Upload...');
            const fileStream = fs.createReadStream(filePath);
            const uploader = new Upload({
                client: s3,
                params: {
                    Bucket: process.env.R2_BUCKET_NAME,
                    Key: fileName,
                    Body: fileStream,
                    ContentType: req.file.mimetype,
                },
                queueSize: 4,
                partSize: 5 * 1024 * 1024,
            });
            await uploader.done();
            const bucketDomain = process.env.R2_CUSTOM_DOMAIN || `https://pub-${process.env.R2_ACCOUNT_ID}.r2.dev`;
            fileUrl = `${bucketDomain}/${fileName}`;
            storageMethod = 'Cloudflare R2';
        } 
        
        // Strategy 2: Supabase Storage
        else if (supabase) {
            console.log('Attempting Supabase Storage Upload...');
            try {
                // Ensure bucket exists
                await supabase.storage.createBucket('media', { public: true });
            } catch (ignored) {} // Might already exist

            const fileBuffer = fs.readFileSync(filePath);
            const { data, error } = await supabase.storage
                .from('media')
                .upload(fileName, fileBuffer, {
                    contentType: req.file.mimetype,
                    upsert: true
                });

            if (error) {
                console.error('Supabase Storage error:', error.message);
                throw error; // Let fallback handle it
            }

            const { data: publicUrlData } = supabase.storage.from('media').getPublicUrl(fileName);
            fileUrl = publicUrlData.publicUrl;
            storageMethod = 'Supabase Storage';
        } 
        
        // Strategy 3: Catbox (Anonymous Zero-config Free Video CDN)
        if (!fileUrl) {
            console.log('Attempting Catbox Fallback Upload...');
            const form = new FormData();
            form.append('reqtype', 'fileupload');
            form.append('fileToUpload', fs.createReadStream(filePath));

            const response = await axios.post('https://catbox.moe/user/api.php', form, {
                headers: form.getHeaders(),
                maxContentLength: Infinity,
                maxBodyLength: Infinity,
            });
            
            fileUrl = response.data; // Catbox returns plain text URL
            storageMethod = 'Catbox.moe';
        }

        // Clean up temp file
        if (fs.existsSync(filePath)) fs.unlinkSync(filePath);
        
        res.status(201).json({
            message: `Media uploaded successfully via ${storageMethod}`,
            media: {
                filename: fileName,
                url: fileUrl,
                mimetype: req.file.mimetype,
                uploadTime: new Date().toISOString()
            }
        });

    } catch (error) {
        console.error('Upload Strategy Failed:', error);
        // Ensure cleanup
        if (fs.existsSync(filePath)) {
            fs.unlinkSync(filePath);
        }
        res.status(500).json({ error: 'All storage strategies failed. Could not upload media.', details: error.message });
    }
});

// Start the server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is successfully running on port ${PORT}`);
});