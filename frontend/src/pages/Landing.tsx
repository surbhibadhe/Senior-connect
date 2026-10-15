import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowUpRight,
  ArrowRight,
  ShieldCheck,
  BookOpen,
  Users,
  Star,
  MessageSquare,
  Sparkles,
  Plus,
  Trash2,
} from 'lucide-react'
import Navbar from '../components/Navbar'
import ReviewModal from '../components/ReviewModal'
import { getReviews, deleteReview } from '../services/reviewService'
import { useAuth } from '../hooks/useAuth'
import type { ReviewResponse } from '../types'

export default function Landing() {
  const { user } = useAuth()
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false)
  const [loadingReviews, setLoadingReviews] = useState(true)

  const fetchCommunityReviews = async () => {
    try {
      setLoadingReviews(true)
      const data = await getReviews()
      setReviews(data || [])
    } catch {
      setReviews([])
    } finally {
      setLoadingReviews(false)
    }
  }

  useEffect(() => {
    fetchCommunityReviews()
  }, [])

  const handleDeleteReview = async (id: number) => {
    if (!window.confirm('Delete this review?')) return
    try {
      await deleteReview(id)
      setReviews((prev) => prev.filter((r) => r.id !== id))
    } catch (err: any) {
      alert(err?.message || 'Could not delete review.')
    }
  }

  const handleReviewCreated = (newReview: ReviewResponse) => {
    setReviews((prev) => [newReview, ...prev])
  }

  return (
    <>
      <Navbar />

      <main style={{ backgroundColor: '#FFFFFF', minHeight: '100vh' }}>
        {/* ========================================================================
            1. HERO SECTION (Screenshot 1)
           ======================================================================== */}
        <section className="hero-reference-section">
          <div className="wrap" style={{ textAlign: 'center', maxWidth: '920px' }}>
            {/* Pill Eyebrow */}
            <div className="hero-pill-badge">
              <span className="pill-dot">●</span>
              <span>THE DESPU STUDENT COLLECTIVE / EST. 2026</span>
            </div>

            {/* Main Headline */}
            <h1 className="hero-display-headline">
              <div>Good notes.</div>
              <div className="headline-green">Even better connections.</div>
            </h1>

            {/* Subtitles */}
            <p className="hero-lead-text">
              Made by DESPU students. <span className="text-forest-green">For DESPU students.</span>
            </p>

            <p className="hero-body-muted">
              The notes you missed. The senior who gets it. The head start you needed.
              <br />
              One shared space to make college a little easier, together.
            </p>

            {/* CTA Buttons */}
            <div className="hero-actions-row">
              <Link to={user ? '/home' : '/signin'} className="btn btn-hero-primary">
                <span>Find your people</span>
                <ArrowUpRight size={17} strokeWidth={2.4} />
              </Link>
              <Link to="/resources" className="btn btn-hero-secondary">
                <span>Explore resources</span>
                <ArrowRight size={16} strokeWidth={2} />
              </Link>
            </div>
          </div>
        </section>

        {/* ========================================================================
            2. PASS IT FORWARD SAGE BANNER SECTION (Screenshot 2)
           ======================================================================== */}
        <section className="wrap" style={{ marginBottom: '4rem' }}>
          <div className="sage-feature-card">
            {/* Left Column */}
            <div className="sage-left-col">
              <div className="sage-eyebrow">KNOWLEDGE DOESN'T END WITH YOUR SEMESTER.</div>
              <h2 className="sage-title">Pass it forward.</h2>
              <p className="sage-desc">One useful note can change someone's entire week.</p>
              <Link to={user ? '/home' : '/signin'} className="sage-link">
                <span>Be someone's head start</span>
                <ArrowUpRight size={17} strokeWidth={2.4} />
              </Link>
            </div>

            {/* Vertical Divider */}
            <div className="sage-divider-vertical" />

            {/* Right Column */}
            <div className="sage-right-col">
              <div className="sage-eyebrow">ONE CAMPUS. MANY CONNECTIONS.</div>

              <div className="sage-connections-grid">
                <div className="sage-connection-item">
                  <BookOpen size={20} strokeWidth={2} className="connection-icon" />
                  <span className="connection-label">Notes worth keeping</span>
                </div>
                <div className="sage-connection-item">
                  <Users size={20} strokeWidth={2} className="connection-icon" />
                  <span className="connection-label">People worth knowing</span>
                </div>
              </div>

              {/* Horizontal Line with Plus */}
              <div className="sage-plus-divider">
                <div className="plus-line" />
                <span className="plus-symbol">+</span>
                <div className="plus-line" />
              </div>

              {/* Bottom Feature */}
              <div className="sage-bottom-row">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                  <Users size={20} strokeWidth={2} className="connection-icon" />
                  <span className="connection-label" style={{ fontWeight: 600 }}>
                    A little further. Together.
                  </span>
                </div>
                <ArrowUpRight size={18} strokeWidth={2.4} className="connection-icon" />
              </div>
            </div>
          </div>

          {/* 4 Bottom Institutional Badges (Screenshot 2) */}
          <div className="institutional-features-row">
            <div className="inst-feature-item">
              <ShieldCheck size={16} strokeWidth={2.2} />
              <span>Exclusively @despu.edu.in</span>
            </div>
            <div className="inst-feature-item">
              <BookOpen size={16} strokeWidth={2.2} />
              <span>Knowledge, without the gatekeeping</span>
            </div>
            <div className="inst-feature-item">
              <Users size={16} strokeWidth={2.2} />
              <span>Every batch. Every branch.</span>
            </div>
            <div className="inst-feature-item">
              <Star size={16} strokeWidth={2.2} />
              <span>Always student-first</span>
            </div>
          </div>
        </section>

        {/* ========================================================================
            3. DIFFERENT YEARS. SAME "BEEN THERE." (Screenshot 3)
           ======================================================================== */}
        <section id="the-idea" className="wrap" style={{ padding: '3.5rem 1.5rem', marginBottom: '4rem' }}>
          <div className="different-years-layout">
            {/* Left Side */}
            <div className="different-years-left">
              <div className="section-eyebrow-dark">NOT ANOTHER COLLEGE GROUP CHAT.</div>
              <h2 className="different-years-headline">
                Different years.
                <br />
                Same “been there.”
              </h2>
              <p className="different-years-desc">
                We've all searched for that one PDF at 2 a.m. Or wished we could ask a senior
                where to even start. Senior Connect makes the good stuff easier to find — and
                the right people easier to reach.
              </p>
              <Link to={user ? '/home' : '/signin'} className="different-years-link">
                <span>There's a place for you here</span>
                <ArrowRight size={16} strokeWidth={2} />
              </Link>
            </div>

            {/* Right Side */}
            <div className="different-years-right">
              {/* Feature 1 */}
              <div className="chat-feature-row">
                <div className="chat-feature-icon-box">
                  <BookOpen size={22} color="var(--primary-green)" strokeWidth={2} />
                </div>
                <div>
                  <h3 className="chat-feature-title">Less searching. More learning.</h3>
                  <p className="chat-feature-desc">
                    Notes, manuals, and practical resources, neatly organized. Not buried under a hundred messages.
                  </p>
                </div>
              </div>

              {/* Feature 2 */}
              <div className="chat-feature-row">
                <div className="chat-feature-icon-box">
                  <MessageSquare size={22} color="var(--primary-green)" strokeWidth={2} />
                </div>
                <div>
                  <h3 className="chat-feature-title">A conversation, not just a download.</h3>
                  <p className="chat-feature-desc">
                    Ask a question, share a different approach, or leave a thank you. Every resource starts a conversation.
                  </p>
                </div>
              </div>

              {/* Feature 3 */}
              <div className="chat-feature-row">
                <div className="chat-feature-icon-box">
                  <Users size={22} color="var(--primary-green)" strokeWidth={2} />
                </div>
                <div>
                  <h3 className="chat-feature-title">One campus. No barriers.</h3>
                  <p className="chat-feature-desc">
                    First year or final year, every student has something to share. That includes you.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ========================================================================
            4. COMMUNITY REVIEWS & PERSPECTIVES (Screenshot 4)
           ======================================================================== */}
        <section id="student-stories" className="stories-sage-container">
          <div className="wrap">
            {/* Header */}
            <div className="stories-header-flex">
              <div>
                <p className="stories-top-caption">The kind of moments we're building this community for.</p>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                {user ? (
                  <button
                    type="button"
                    className="btn btn-sm btn-primary"
                    onClick={() => setIsReviewModalOpen(true)}
                  >
                    <Plus size={14} /> Write a Review
                  </button>
                ) : (
                  <Link to="/signin" className="btn btn-sm btn-outline" style={{ background: '#FFFFFF' }}>
                    Sign in to Review
                  </Link>
                )}
                <div className="better-together-pill">
                  <Sparkles size={14} />
                  <span>Better, together</span>
                </div>
              </div>
            </div>

            {/* Loading State */}
            {loadingReviews && (
              <div className="stories-cards-grid">
                {[1, 2, 3].map((n) => (
                  <div key={n} className="story-perspective-card" style={{ opacity: 0.7 }}>
                    <div className="skeleton" style={{ height: '18px', width: '35%', marginBottom: '1.25rem' }} />
                    <div className="skeleton" style={{ height: '14px', width: '90%', marginBottom: '0.5rem' }} />
                    <div className="skeleton" style={{ height: '14px', width: '80%', marginBottom: '1.5rem' }} />
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <div className="skeleton" style={{ width: '36px', height: '36px', borderRadius: '50%' }} />
                      <div style={{ flex: 1 }}>
                        <div className="skeleton" style={{ height: '14px', width: '50%', marginBottom: '0.35rem' }} />
                        <div className="skeleton" style={{ height: '12px', width: '35%' }} />
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Empty State — No dummy data, authentic prompt to write first review */}
            {!loadingReviews && reviews.length === 0 && (
              <div
                style={{
                  backgroundColor: '#FFFFFF',
                  border: '1px solid var(--border)',
                  borderRadius: '16px',
                  padding: '3rem 2rem',
                  textAlign: 'center',
                  maxWidth: '600px',
                  margin: '0 auto',
                  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.02)',
                }}
              >
                <div
                  style={{
                    width: '46px',
                    height: '46px',
                    borderRadius: '50%',
                    backgroundColor: 'var(--pastel-sage)',
                    color: 'var(--primary-green)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    margin: '0 auto 1.15rem',
                  }}
                >
                  <Sparkles size={22} />
                </div>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 700, margin: '0 0 0.5rem', color: 'var(--text-main)' }}>
                  No student reviews yet
                </h3>
                <p style={{ fontSize: '0.92rem', color: 'var(--text-secondary)', lineHeight: 1.6, margin: '0 0 1.5rem' }}>
                  Be the first student to share your experience with SeniorConnect. Real feedback from seniors and juniors helps the whole DESPU campus learn together.
                </p>
                {user ? (
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setIsReviewModalOpen(true)}
                  >
                    <Plus size={16} /> Share Your Review
                  </button>
                ) : (
                  <Link to="/signin" className="btn btn-primary">
                    Sign in to Write the First Review ↗
                  </Link>
                )}
              </div>
            )}

            {/* Real Reviews Cards Grid */}
            {!loadingReviews && reviews.length > 0 && (
              <div className="stories-cards-grid">
                {reviews.map((rev, idx) => (
                  <div key={rev.id || idx} className="story-perspective-card">
                    {/* Green Stars */}
                    <div className="story-stars-row">
                      {'★'.repeat(rev.rating || 5)}
                    </div>

                    {/* Quote */}
                    <p className="story-quote-text">
                      "{rev.content}"
                    </p>

                    {/* Footer Tag */}
                    <div className="story-card-footer">
                      <div className="story-author-group">
                        {rev.authorPicture ? (
                          <img
                            src={rev.authorPicture}
                            alt={rev.authorName}
                            style={{
                              width: '36px',
                              height: '36px',
                              borderRadius: '50%',
                              objectFit: 'cover',
                              border: '1px solid var(--border)',
                            }}
                          />
                        ) : (
                          <div className="story-num-circle">
                            {rev.perspectiveTag || String(idx + 1).padStart(2, '0')}
                          </div>
                        )}
                        <div>
                          <div className="story-author-title">{rev.authorName}</div>
                          <div className="story-author-sub">{rev.authorSubtitle || 'DESPU Student'}</div>
                        </div>
                      </div>

                      {rev.canDelete && (
                        <button
                          type="button"
                          className="btn btn-xs btn-outline"
                          style={{ color: 'var(--danger)', borderColor: 'transparent' }}
                          onClick={() => handleDeleteReview(rev.id)}
                          title="Delete review"
                        >
                          <Trash2 size={13} />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}

            <p className="stories-bottom-note">
              Community perspectives from verified students of DES Pune University.
            </p>
          </div>
        </section>

        {/* ========================================================================
            5. YOUR NEXT CHAPTER STARTS WITH A CONNECTION (Screenshot 4)
           ======================================================================== */}
        <section className="dark-cta-section">
          <div className="wrap" style={{ textAlign: 'center', maxWidth: '780px' }}>
            <h2 className="dark-cta-headline">
              Your next chapter starts with a connection.
            </h2>
            <p className="dark-cta-subhead">
              Bring your curiosity. Bring your notes. We'll bring the community.
            </p>

            <Link to={user ? '/home' : '/signin'} className="btn btn-dark-cta-action">
              <span>Join Senior Connect</span>
              <ArrowUpRight size={17} strokeWidth={2.4} />
            </Link>

            <div className="dark-cta-note">
              <span>✓ Your college email is your way in.</span>
            </div>
          </div>
        </section>
      </main>

      {/* ========================================================================
          6. FOOTER (Screenshot 4)
         ======================================================================== */}
      <footer className="footer-reference">
        <div className="wrap footer-content-row">
          <Link to="/" className="brand" style={{ gap: '0.5rem' }}>
            <div className="brand-logo-icon">
              <span style={{ fontSize: '14px', lineHeight: 1 }}>🎓</span>
            </div>
            <div className="brand-name">
              <span className="brand-senior">senior</span>{' '}
              <span className="brand-connect">connect</span>
              <span className="brand-arrow">↗</span>
            </div>
          </Link>

          <div className="footer-middle-text">
            Made with 🤍 at DESPU. A little help goes a long way.
          </div>

          <div className="footer-copyright">
            © 2026 Senior Connect
          </div>
        </div>
      </footer>

      {/* Write a review modal */}
      <ReviewModal
        isOpen={isReviewModalOpen}
        onClose={() => setIsReviewModalOpen(false)}
        onSuccess={handleReviewCreated}
      />
    </>
  )
}
