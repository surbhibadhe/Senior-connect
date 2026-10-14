import { useState } from 'react'
import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { useAuth } from './hooks/useAuth'
import Navbar from './components/Navbar'
import UploadModal from './components/UploadModal'
import Landing from './pages/Landing'
import SignIn from './pages/SignIn'
import Home from './pages/Home'
import Browse from './pages/Browse'
import ResourceDetail from './pages/ResourceDetail'
import Bookmarks from './pages/Bookmarks'
import Profile from './pages/Profile'
import ContributorProfile from './pages/ContributorProfile'
import AdminModeration from './pages/AdminModeration'

function ProtectedLayout() {
  const { user, loading } = useAuth()
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false)

  if (loading) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'var(--bg-main)',
        }}
      >
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.96rem' }}>
          Connecting to SeniorConnect…
        </p>
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/signin" replace />
  }

  return (
    <>
      <Navbar onOpenUpload={() => setIsUploadModalOpen(true)} />
      <Outlet />
      <UploadModal
        isOpen={isUploadModalOpen}
        onClose={() => setIsUploadModalOpen(false)}
        onSuccess={() => {
          // If on a page that needs full reload or event, navigate or let component handle
          window.location.reload()
        }}
      />
    </>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/signin" element={<SignIn />} />

      {/* Protected Student Routes */}
      <Route element={<ProtectedLayout />}>
        <Route path="/home" element={<Home />} />
        <Route path="/resources" element={<Browse />} />
        <Route path="/resources/:id" element={<ResourceDetail />} />
        <Route path="/bookmarks" element={<Bookmarks />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/users/:id" element={<ContributorProfile />} />
        <Route path="/admin" element={<AdminModeration />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
