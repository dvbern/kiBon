import path from 'path';
import fs from 'fs';
import {
    createProxyMiddleware,
    responseInterceptor
} from 'http-proxy-middleware';

export default {
    '/ebegu': {
        target: 'http://localhost:8080/',
        secure: false,
        proxyTimeout: 240000,
        on: {
            onProxyReq(proxyReq, req, res) {
                customSetHeader(proxyReq);
            },
            onProxyRes(proxyReq, req, res) {
                customSetHeader(proxyReq);
            }
        }
    },
    '/assets/translations/gemeinde': {
        target: 'http://localhost:8080/',
        secure: false,
        on: {
            onProxyReq(proxyReq, req, res) {
                customSetHeader(proxyReq);
            },
            onProxyRes(proxyReq, req, res) {
                customSetHeader(proxyReq);
            }
        },

        context: ['/assets/translations/gemeinde'],

        onProxyReq: (proxyReq, req, res) => {
            const requestedFile = path.join(
                path.resolve(path.dirname('')),
                'src',
                req.url
            );

            // Check if the requested file exists
            if (fs.existsSync(requestedFile)) {
                // Serve the file directly
                res.writeHead(200, {'Content-Type': 'text/plain'}); // You can adjust content type as needed
                fs.createReadStream(requestedFile).pipe(res);
            } else {
                // If file doesn't exist, return 204 No Content
                res.writeHead(204);
                res.end('');
            }
        }
    }
};

function customSetHeader(proxyReq) {
    proxyReq.setHeader(
        'Content-Security-Policy',
        "default-src 'none'; object-src 'self'; script-src 'self' 'unsafe-eval' ajax.googleapis.com; connect-src 'self' ws:; img-src 'self' data:; style-src 'self' 'unsafe-inline'  https://fonts.googleapis.com; font-src 'self'  https://fonts.googleapis.com https://fonts.gstatic.com;"
    );
    proxyReq.setHeader('X-Frame-Options', 'DENY');
    proxyReq.setHeader('X-XSS-Protection', '1; mode=block');
    proxyReq.setHeader('X-Content-Type-Options', 'nosniff');
    proxyReq.setHeader('Referrer-Policy', 'strict-origin');
}
