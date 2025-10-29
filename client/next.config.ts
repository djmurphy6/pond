import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    /* config options here */
    images: {
        domains: [
            'example.com',
            'images.unsplash.com',
            'api.dicebear.com',
            process.env.NEXT_PUBLIC_SUPABASE_DOMAIN || '',
        ].filter(Boolean), // filter out empty strings
    },
};

export default nextConfig;
