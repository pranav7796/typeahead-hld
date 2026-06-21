/** @type {import('next').NextConfig} */
const backendUrl = process.env.BACKEND_URL || "http://localhost:8080";

const nextConfig = {
  // Proxy API calls to the Spring Boot backend so the browser stays same-origin
  // (avoids CORS). In Docker, BACKEND_URL points at the backend service.
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${backendUrl}/api/:path*`,
      },
    ];
  },
};

module.exports = nextConfig;
