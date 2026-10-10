import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { GraduationCap, Lock, Mail, ArrowRight, AlertCircle, ShieldAlert, UserCheck, CheckCircle2 } from 'lucide-react';

const Login = () => {
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState(location.state?.registeredEmail || '');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState(location.state?.successMessage || '');

  const redirectPath = location.state?.from?.pathname || '/dashboard';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');

    const cleanEmail = email ? email.trim() : '';
    const cleanPassword = password ? password.trim() : '';

    if (!cleanEmail || !cleanPassword) {
      setError('Please provide both email and password.');
      return;
    }

    const res = await login(cleanEmail, cleanPassword);
    if (res.success) {
      if (res.user.role === 'ROLE_ADMIN') {
        navigate('/admin');
      } else {
        navigate(redirectPath === '/' ? '/dashboard' : redirectPath);
      }
    } else {
      setError(res.error);
    }
  };

  const fillDemo = (role) => {
    if (role === 'student') {
      setEmail('student@interviewprep.com');
      setPassword('Student@123');
    }
  };

  return (
    <div className="min-h-[88vh] flex items-center justify-center py-6 sm:py-12 px-3.5 sm:px-6 lg:px-8 bg-gradient-to-br from-slate-50 via-indigo-50/40 to-slate-100 relative overflow-hidden">
      {/* Background Animated Ambient Lights */}
      <div className="absolute -top-24 -left-24 w-80 h-80 bg-indigo-500/15 rounded-full blur-3xl pointer-events-none animate-pulse-glow" />
      <div className="absolute -bottom-24 -right-24 w-80 h-80 bg-violet-500/15 rounded-full blur-3xl pointer-events-none animate-pulse-glow" style={{ animationDelay: '1.5s' }} />

      <div className="relative max-w-md w-full space-y-6 sm:space-y-7 bg-white/95 backdrop-blur-md p-5 sm:p-10 rounded-2xl sm:rounded-3xl shadow-xl shadow-indigo-950/5 border border-slate-200/90 animate-fade-in-up">
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 rounded-2xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white shadow-lg shadow-indigo-500/25 animate-float mb-1 sm:mb-2">
            <GraduationCap className="w-7 h-7 sm:w-8 sm:h-8" />
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Welcome back
          </h2>
          <p className="text-xs text-slate-500">
            Sign in to resume mock preparations and track performance metrics
          </p>
        </div>

        {/* 1-Click Demo for Student Testing */}
        <div className="p-3.5 rounded-2xl bg-gradient-to-r from-indigo-50/80 to-violet-50/50 border border-indigo-100/90 flex flex-col sm:flex-row sm:items-center justify-between gap-2.5 shadow-xs">
          <div>
            <p className="text-xs font-semibold text-indigo-950 flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              <UserCheck className="w-3.5 h-3.5 text-indigo-600" /> Student Demo Access
            </p>
            <p className="text-[11px] text-slate-500">1-click login for student mock practice</p>
          </div>
          <button
            type="button"
            onClick={() => fillDemo('student')}
            className="w-full sm:w-auto py-2 sm:py-1.5 px-3 rounded-xl sm:rounded-lg bg-white border border-indigo-200 hover:border-indigo-400 hover:bg-indigo-50/80 text-xs font-semibold text-indigo-700 shadow-xs hover:shadow-sm hover:scale-[1.02] active:scale-100 transition-all shrink-0 cursor-pointer text-center"
          >
            Autofill Student
          </button>
        </div>

        {successMessage && !error && (
          <div className="p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs flex items-center gap-2 animate-scale-in">
            <CheckCircle2 className="w-4 h-4 shrink-0 text-emerald-600" />
            <span>{successMessage}</span>
          </div>
        )}

        {error && error === 'ACCOUNT_NOT_VERIFIED' ? (
          <div className="p-3.5 rounded-xl bg-orange-50 border border-orange-200 text-orange-700 text-xs flex flex-col gap-2 animate-scale-in">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span className="font-semibold">Your email is not verified.</span>
            </div>
            <p className="pl-6">
              Please go to the <Link to="/register" className="font-bold underline text-orange-800">Register</Link> page and submit your details again to receive a new Verification OTP.
            </p>
          </div>
        ) : error && (
          <div className="p-3.5 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2 animate-scale-in">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
              Email Address
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Mail className="w-4 h-4" />
              </div>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@college.edu"
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 text-sm transition-all duration-200"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
              Password
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Lock className="w-4 h-4" />
              </div>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 text-sm transition-all duration-200"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="group w-full py-3.5 px-4 rounded-xl bg-gradient-to-r from-indigo-600 via-indigo-700 to-violet-700 hover:from-indigo-500 hover:to-violet-600 disabled:opacity-60 text-white font-bold text-sm shadow-lg shadow-indigo-600/25 hover:shadow-indigo-600/35 hover:-translate-y-0.5 active:translate-y-0 flex items-center justify-center gap-2 transition-all duration-200 cursor-pointer mt-6"
          >
            {loading ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            ) : (
              <>
                <span>Sign In to Platform</span>
                <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
              </>
            )}
          </button>
        </form>

        <div className="pt-4 border-t border-slate-100 text-center">
          <p className="text-xs text-slate-600">
            Don't have an account?{' '}
            <Link to="/register" className="font-bold text-indigo-600 hover:text-indigo-700 hover:underline">
              Create an account
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
