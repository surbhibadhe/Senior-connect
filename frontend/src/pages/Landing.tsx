import { Link } from 'react-router-dom'
import {
  BookOpen,
  FolderGit2,
  Share2,
  FileCheck2,
  Sparkles,
} from 'lucide-react'
import Navbar from '../components/Navbar'
import { CATEGORY_LABELS } from '../types'

export default function Landing() {
  return (
    <>
      <Navbar />

      <main>
        {/* Editorial Hero */}
        <section className="landing-hero">
          <div className="wrap">
            <div className="institution-pill">
              <Sparkles size={14} color="var(--accent-terracotta)" />
              <span>DES Pune University Student Academic Repository</span>
            </div>

            <h1 className="hero-headline">
              Knowledge deepens when <em>seniors share</em>.
            </h1>

            <p className="hero-subhead">
              SeniorConnect is a curated, student-powered academic shelf for DES Pune
              University. Access verified lecture notes, lab manuals, assignments, and
              previous years’ question papers passed down from preceding batches.
            </p>

            <div className="hero-cta-group">
              <Link to="/signin" className="btn btn-primary">
                Sign in with College Account
              </Link>
              <a href="#about" className="btn btn-secondary">
                Learn how it works
              </a>
            </div>
          </div>
        </section>

        {/* How SeniorConnect Works */}
        <section id="about" className="landing-section">
          <div className="wrap">
            <h2 className="landing-section-title">A shared shelf for every semester.</h2>
            <p className="landing-section-subhead">
              Every academic year, students create valuable study notes and solve challenging
              lab experiments. SeniorConnect preserves that hard work so no student has to
              start from scratch.
            </p>

            <div className="feature-grid">
              <div className="feature-card">
                <div className="feature-step-num">01</div>
                <h3 className="feature-title">Verified University Access</h3>
                <p className="feature-desc">
                  Authentication is restricted strictly to verified <code>@despu.edu.in</code>{' '}
                  accounts via Google OAuth, ensuring a safe, trusted collegiate network.
                </p>
              </div>

              <div className="feature-card">
                <div className="feature-step-num">02</div>
                <h3 className="feature-title">Structured Course Materials</h3>
                <p className="feature-desc">
                  Browse and filter by semester categories: lecture notes, comprehensive lab
                  manuals, practical assignments, and previous exam question papers.
                </p>
              </div>

              <div className="feature-card">
                <div className="feature-step-num">03</div>
                <h3 className="feature-title">Peer Recognition & Community</h3>
                <p className="feature-desc">
                  Contribute your own study materials, track helpfulness feedback from juniors,
                  and build a recognized academic contributor profile.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Categories Showcase */}
        <section className="landing-section" style={{ backgroundColor: 'var(--bg-main)' }}>
          <div className="wrap">
            <h2 className="landing-section-title">What you'll discover.</h2>
            <p className="landing-section-subhead">
              Carefully organized academic resources across foundational disciplines and specialized courses.
            </p>

            <div className="categories-showcase">
              {Object.entries(CATEGORY_LABELS).map(([key, label]) => (
                <div key={key} className="category-chip">
                  <BookOpen size={18} color="var(--accent-terracotta)" />
                  <span>{label}</span>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Call to action section */}
        <section className="landing-section" style={{ backgroundColor: 'var(--bg-secondary)', textAlign: 'center' }}>
          <div className="wrap">
            <h2 className="landing-section-title" style={{ maxWidth: '32rem', margin: '0 auto 1rem' }}>
              Ready to learn from your seniors?
            </h2>
            <p className="landing-section-subhead" style={{ maxWidth: '30rem', margin: '0 auto 2rem' }}>
              Sign in with your DES Pune University Google account to explore resources or share what you’ve learned.
            </p>
            <Link to="/signin" className="btn btn-primary">
              Continue with Google Account
            </Link>
          </div>
        </section>
      </main>

      <footer className="footer">
        <div className="wrap footer-content">
          <div>
            <strong style={{ color: 'var(--text-main)', fontFamily: 'var(--serif)' }}>
              SeniorConnect
            </strong>{' '}
            · DES Pune University Academic Platform
          </div>
          <div style={{ fontSize: '0.82rem' }}>
            Built for students, verified by university authentication.
          </div>
        </div>
      </footer>
    </>
  )
}
