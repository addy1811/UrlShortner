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

        <Route path="/f/:linkId" element={<PublicFormPage />} />

        <Route path="/r/:code" element={<RedirectResolverPage />} />
        
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
