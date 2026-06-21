const express = require('express');
const multer = require('multer');
const cors = require('cors');
const { S3Client } = require('@aws-sdk/client-s3');
const { Upload } = require('@aws-sdk/lib-storage');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

// Initialize Express app
const app = express();
app.use(cors());
app.use(express.json());

// Cloudflare R2 Configuration
const s3 = new S3Client({
    region: 'auto',
    endpoint: `https://${process.env.R2_ACCOUNT_ID}.r2.cloudflarestorage.com`,
    credentials: {
        accessKeyId: process.env.R2_ACCESS_KEY_ID || '',
        secretAccessKey: process.env.R2_SECRET_ACCESS_KEY || '',
    },
});

// Configure multer for DISK storage (Fixes OOM crashes for large videos)
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
    limits: { fileSize: 500 * 1024 * 1024 }, // 500MB limit for large videos
});

// --- API ROUTES ---

// Health Check Endpoint
app.get('/api/health', (req, res) => {
    res.json({ status: 'ok', message: 'Backend server is running properly with Optimized R2 Uploads!' });
});

// Proxy logic to keep Supabase API key hidden
const REAL_SUPABASE_URL = process.env.SUPABASE_URL; // e.g., https://your-project.supabase.co
const REAL_SUPABASE_KEY = process.env.SUPABASE_KEY;

app.use(['/auth/v1', '/rest/v1'], async (req, res) => {
    try {
        if (!REAL_SUPABASE_URL || !REAL_SUPABASE_KEY || REAL_SUPABASE_KEY === "proxy-will-append-real-key" || REAL_SUPABASE_URL === "https://recharge-api-backend.onrender.com") {
            return res.status(500).json({ 
                error: 'server_error', 
                error_description: 'Backend Configuration Error: You must set REAL Supabase URL and SUPABASE_KEY in your Render.com Environment Variables!'
            });
        }

        // Fix double trailing slashes just in case, originalUrl contains /auth/v1/...
        const baseUrl = REAL_SUPABASE_URL.endsWith('/') ? REAL_SUPABASE_URL.slice(0, -1) : REAL_SUPABASE_URL;
        const targetUrl = new URL(req.originalUrl, baseUrl).toString();
        
        const headers = new Headers();
        // Copy relevant headers from the client
        if (req.headers.authorization) {
            headers.append('Authorization', req.headers.authorization);
        } else {
            headers.append('Authorization', `Bearer ${REAL_SUPABASE_KEY}`);
        }
        
        headers.append('apikey', REAL_SUPABASE_KEY);
        // Ensure X-Client-Info matches
        if (req.headers['x-client-info']) headers.append('X-Client-Info', req.headers['x-client-info']);
        if (req.headers['content-type']) headers.append('Content-Type', req.headers['content-type']);
        if (req.headers.accept) headers.append('Accept', req.headers.accept);

        const fetchOptions = {
            method: req.method,
            headers: headers,
        };

        if (['POST', 'PUT', 'PATCH'].includes(req.method)) {
            // Forward body if present
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

// Media Upload Endpoint (Video/Photo) with Retry and Large File Support
app.post('/api/upload', upload.single('media'), async (req, res) => {
    if (!req.file) {
        return res.status(400).json({ error: 'No media file provided or invalid format.' });
    }
    
    const filePath = req.file.path;
    const fileName = req.file.filename;

    try {
        const fileStream = fs.createReadStream(filePath);
        
        // Use lib-storage Upload for streaming large chunks to S3/R2 efficiently
        const uploader = new Upload({
            client: s3,
            params: {
                Bucket: process.env.R2_BUCKET_NAME,
                Key: fileName,
                Body: fileStream,
                ContentType: req.file.mimetype,
            },
            queueSize: 4, // Concurrent upload parts
            partSize: 5 * 1024 * 1024, // 5MB chunk size
        });

        // Event for upload progress
        uploader.on('httpUploadProgress', (progress) => {
            console.log(`Upload Progress: ${progress.loaded} / ${progress.total}`);
        });

        await uploader.done();
        
        // Clean up temp file
        fs.unlinkSync(filePath);

        const bucketDomain = process.env.R2_CUSTOM_DOMAIN || `https://pub-${process.env.R2_ACCOUNT_ID}.r2.dev`;
        const fileUrl = `${bucketDomain}/${fileName}`;
        
        res.status(201).json({
            message: 'Media uploaded successfully to R2',
            media: {
                filename: fileName,
                url: fileUrl,
                mimetype: req.file.mimetype,
                uploadTime: new Date().toISOString()
            }
        });
    } catch (error) {
        console.error('R2 Upload Error:', error);
        // Clean up temp file in case of error
        if (fs.existsSync(filePath)) {
            fs.unlinkSync(filePath);
        }
        res.status(500).json({ error: 'Failed to upload media to Cloudflare R2 server.', details: error.message });
    }
});

// Start the server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is successfully running on port ${PORT}`);
    console.log(`Ready for Render deployment with Cloudflare R2!`);
});