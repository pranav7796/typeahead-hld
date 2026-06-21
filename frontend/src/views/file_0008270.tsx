import { SearchBar } from "@/components/SearchBar";

// Page-level view. Next.js reserves the `pages/` directory name for its legacy
// router, so App Router projects keep route components in `app/` and view
// components here.
export function HomePage() {
  return (
    <>
      {/* Background Animated Gradient Glowing Blobs */}
      <div className="bg-mesh">
        <div className="blob blob-1"></div>
        <div className="blob blob-2"></div>
        <div className="blob blob-3"></div>
      </div>

      <main className="page">
        <header className="header">
          <h1 className="title">TypeAhead</h1>
          <p className="subtitle">
            A distributed prefix autocomplete system serving real search suggestions from memory caches.
          </p>
        </header>

        {/* Search Widget */}
        <SearchBar />

        {/* Real-time System Statistics */}
        <footer className="info-footer">
          <div className="info-card">
            <div className="info-card-val">1M+</div>
            <div className="info-card-lbl">Queries Indexed</div>
          </div>
          <div className="info-card-val-container info-card">
            <div className="info-card-val">&lt; 50ms</div>
            <div className="info-card-lbl">Target Latency</div>
          </div>
          <div className="info-card">
            <div className="info-card-val">3x</div>
            <div className="info-card-lbl">Redis Cache Nodes</div>
          </div>
        </footer>
      </main>
    </>
  );
}
