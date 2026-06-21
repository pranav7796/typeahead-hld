import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "TypeAhead",
  description: "Distributed search typeahead system",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
