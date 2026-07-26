import { Routes, Route, Navigate } from 'react-router';
import Navbar from '@/components/layout/Navbar';
import ProtectedRoute from '@/components/layout/ProtectedRoute';
import HomePage from '@/pages/HomePage';
import LoginPage from '@/pages/LoginPage';
import RegisterPage from '@/pages/RegisterPage';
import DashboardPage from '@/pages/DashboardPage';
import LinkDetailPage from '@/pages/LinkDetailPage';
import FormBuilderPage from '@/pages/FormBuilderPage';
import PublicFormPage from '@/pages/PublicFormPage';
import RedirectResolverPage from '@/pages/RedirectResolverPage';
export default function App() {
  return (
    <>
      <Navbar />

      <Routes>
         <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Public - anonymous visitors fill this out, no auth required.
            Mirrors the backend's permitAll rules on GET/POST /links/{id}/form */}
        <Route path="/f/:linkId" element={<PublicFormPage />} />

           {/* Public - see RedirectResolverPage's comment for why this exists
            as a frontend page rather than a raw link to the backend. */}
        <Route path="/r/:code" element={<RedirectResolverPage />} />
        
        {/* Owner-only routes, gated by ProtectedRoute */}
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/links/:linkId" element={<LinkDetailPage />} />
          <Route path="/links/:linkId/form-builder" element={<FormBuilderPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </>
  );
}
