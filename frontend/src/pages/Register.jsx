import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../services/api';
import { GraduationCap, Lock, Mail, User, Building, BookOpen, Calendar, ArrowRight, AlertCircle, CheckCircle2 } from 'lucide-react';

const Register = () => {
  const { register, loading } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    college: '',
    degree: 'B.Tech',
    branch: 'Computer Science',
    graduationYear: 2025,
  });

  const [error, setError] = useState('');
  const [isOtpPhase, setIsOtpPhase] = useState(false);
  const [otp, setOtp] = useState('');
  const [verifying, setVerifying] = useState(false);
  const [resending, setResending] = useState(false);
  const [message, setMessage] = useState('');

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const res = await register(formData);
    if (res.success) {
      setIsOtpPhase(true);
      setMessage('OTP has been sent to your email. Please check your inbox.');
    } else {
      setError(res.error);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setError('');
    setVerifying(true);
    try {
      await authApi.verifyOtp({ email: formData.email, otp });
      navigate('/login', {
        state: {
          registeredEmail: formData.email,
          successMessage: 'Email verified successfully! Please sign in.',
        },
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid OTP');
    } finally {
      setVerifying(false);
    }
  };

  const handleResendOtp = async () => {
    setError('');
    setMessage('');
    setResending(true);
    try {
      await authApi.resendOtp(formData.email);
      setMessage('A new OTP has been sent to your email.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend OTP');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="min-h-[90vh] flex items-center justify-center py-6 sm:py-12 px-3.5 sm:px-6 lg:px-8 bg-gradient-to-br from-slate-50 via-indigo-50/40 to-slate-100 relative overflow-hidden">
      {/* Background Animated Ambient Lights */}
      <div className="absolute -top-28 -left-28 w-96 h-96 bg-indigo-500/15 rounded-full blur-3xl pointer-events-none animate-pulse-glow" />
      <div className="absolute -bottom-28 -right-28 w-96 h-96 bg-violet-500/15 rounded-full blur-3xl pointer-events-none animate-pulse-glow" style={{ animationDelay: '1.5s' }} />

      <div className="relative max-w-xl w-full space-y-6 sm:space-y-7 bg-white/95 backdrop-blur-md p-5 sm:p-10 rounded-2xl sm:rounded-3xl shadow-xl shadow-indigo-950/5 border border-slate-200/90 animate-fade-in-up">
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 rounded-2xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white shadow-lg shadow-indigo-500/25 animate-float mb-1 sm:mb-2">
            <GraduationCap className="w-7 h-7 sm:w-8 sm:h-8" />
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Create Student Profile
          </h2>
          <p className="text-xs text-slate-500">
            Join the technical preparation platform and unlock real-time mock assessments
          </p>
        </div>

        {error && (
          <div className="p-3.5 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center gap-2 animate-scale-in">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {message && (
          <div className="p-3.5 rounded-xl bg-green-50 border border-green-200 text-green-700 text-xs flex items-center gap-2 animate-scale-in">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{message}</span>
          </div>
        )}

        {!isOtpPhase ? (
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">Full Name</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <User className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  name="name"
                  required
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="e.g. John Doe"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 text-sm"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">Email Address</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  type="email"
                  name="email"
                  required
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="john@example.com"
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 text-sm"
                />
              </div>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">Password</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Lock className="w-4 h-4" />
              </div>
              <input
                type="password"
                name="password"
                required
                minLength={6}
                value={formData.password}
                onChange={handleChange}
                placeholder="At least 6 characters"
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 text-sm"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">College / University</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Building className="w-4 h-4" />
              </div>
              <input
                type="text"
                name="college"
                required
                value={formData.college}
                onChange={handleChange}
                placeholder="e.g. National Institute of Tech"
                className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 text-sm"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">Degree</label>
              <input
                type="text"
                name="degree"
                value={formData.degree}
                onChange={handleChange}
                placeholder="B.Tech, B.S., MCA"
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:border-indigo-600 focus:outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">Branch</label>
              <input
                type="text"
                name="branch"
                value={formData.branch}
                onChange={handleChange}
                placeholder="CSE, IT, ECE"
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:border-indigo-600 focus:outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">Grad Year</label>
              <input
                type="number"
                name="graduationYear"
                value={formData.graduationYear}
                onChange={handleChange}
                min={2020}
                max={2032}
                className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:border-indigo-600 focus:outline-none"
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
                <span>Create Account</span>
                <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
              </>
            )}
          </button>
        </form>
        ) : (
        <form onSubmit={handleVerifyOtp} className="space-y-4">
          <div className="text-center pb-2">
            <p className="text-sm text-slate-600">Enter the 6-digit code sent to <strong className="text-slate-800">{formData.email}</strong></p>
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">Verification Code</label>
            <input
              type="text"
              name="otp"
              required
              maxLength={6}
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              placeholder="000000"
              className="w-full px-4 py-3 rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 text-center text-xl tracking-[0.5em] font-semibold"
            />
          </div>
          <button
            type="submit"
            disabled={verifying || otp.length !== 6}
            className="group w-full py-3.5 px-4 rounded-xl bg-gradient-to-r from-indigo-600 via-indigo-700 to-violet-700 hover:from-indigo-500 hover:to-violet-600 disabled:opacity-60 text-white font-bold text-sm shadow-lg shadow-indigo-600/25 hover:shadow-indigo-600/35 flex items-center justify-center gap-2 transition-all duration-200 mt-6"
          >
            {verifying ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            ) : (
              <span>Verify Email</span>
            )}
          </button>
          <div className="text-center pt-2">
            <button
              type="button"
              onClick={handleResendOtp}
              disabled={resending}
              className="text-xs font-medium text-indigo-600 hover:text-indigo-800 disabled:text-slate-400"
            >
              {resending ? 'Sending...' : 'Didn\'t receive code? Resend OTP'}
            </button>
          </div>
        </form>
        )}

        <div className="pt-4 border-t border-slate-100 text-center">
          <p className="text-xs text-slate-600">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-indigo-600 hover:text-indigo-700">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
