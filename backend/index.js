const express = require('express');
const multer = require('multer');
const cors = require('cors');
const { S3Client, PutObjectCommand } = require('@aws-sdk/client-s3');
const { v4: uuidv4 } = require('uuid');
const serverless = require('serverless-http');

// Simple obfuscation function for environment variables
const atob = (b64) => Buffer.from(b64, 'base64').toString('utf-8');

// The keys below will be read from environment variables on Render/Netlify
// But we use an obfuscated approach for key names if you want to hardcode (NOT RECOMMENDED)
// Better to just use environment variables securely.

// Provide your actual R2 Details via Environment Variables on Render/Netlify:
// R2_ACC_ID = 04fcb334fa07a6aa40a8160b776e0d8d
// R2_ACC_KEY = 68f7a4461cec575294a66b9be9d99183a39e255c90d55e7dff6e2a7839496b6e
// R2_SEC_KEY = 89b86d8f598129dae2bed28527c7e5f2

// Hardcoding is generally unsafe, but as requested we will put them in a slightly 
// obfuscated manner.
const _A_ID = atob("MDRmY2IzMzRmYTA3YTZhYTQwYTgxNjBiNzc2ZTBkOGQ="); 
const _A_K = atob("NjhmN2E0NDYxY2VjNTc1Mjk0YTY2YjliZTlkOTkxODNhMzllMjU1YzkwZDU1ZTdkZmY2ZTJhNzgzOTQ5NmI2ZQ==");
const _S_K = atob("ODliODZkOGY1OTgxMjlkYWUyYmVkMjg1MjdjN2U1ZjI=");
const _PUB_URL = atob("aHR0cHM6Ly9wdWItMDRmY2IzMzRmYTA3YTZhYTQwYTgxNjBiNzc2ZTBkOGQucjIuZGV2"); // Assuming default public R2 dev URL, UPDATE IF CUSTOM DOMAIN
const _BUCKET = "media"; // UPDATE THIS IF BUCKET IS DIFFERENT

// Initialize Express app
const app = express();
app.use(cors());
app.use(express.json());

// Handle Netlify's serverless environment correctly with Multer (memory storage)
const storage = multer.memoryStorage();
const upload = multer({ 
    storage: storage,
    limits: { fileSize: 200 * 1024 * 1024 }, // 200MB
});

const s3 = new S3Client({
    region: 'auto',
    endpoint: `https://${_A_ID}.r2.cloudflarestorage.com`,
    credentials: {
        accessKeyId: _A_K,
        secretAccessKey: _S_K,
    },
});

app.get('/api/health', (req, res) => {
    res.json({ status: 'ok', message: 'Backend server is running with Cloudflare R2' });
});

// Media Upload Endpoint
app.post('/api/upload', upload.single('media'), async (req, res) => {
    if (!req.file) {
        return res.status(400).json({ error: 'No media file provided.' });
    }
    
    // Obfuscated execution logic
    const ext = req.file.originalname.split('.').pop();
    const uniqueName = `upload_${Date.now()}_${uuidv4()}.${ext}`;

    try {
        const cmd = new PutObjectCommand({
            Bucket: _BUCKET,
            Key: uniqueName,
            Body: req.file.buffer,
            ContentType: req.file.mimetype,
        });

        await s3.send(cmd);
        
        // Final public URL
        const fileUrl = `${_PUB_URL}/${uniqueName}`;
        
        res.status(201).json({
            message: `Media uploaded successfully`,
            media: { url: fileUrl }
        });

    } catch (e) {
        console.error('R2 Upload Failed:', e);
        res.status(500).json({ error: 'Storage failed', details: e.message });
    }
});

if (process.env.NETLIFY || process.env.AWS_EXECUTION_ENV) {
    module.exports.handler = serverless(app);
} else {
    const PORT = process.env.PORT || 3000;
    app.listen(PORT, () => {
        console.log(`Server is running on port ${PORT}`);
    });
}