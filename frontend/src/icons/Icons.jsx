import React from 'react';

export const MdDelete = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M16 9v10H8V9h8m-1.5-6H9.5l-1 1H5v2h14V4h-3.5l-1-1z" />
  </svg>
);

export const MdStar = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z" />
  </svg>
);

export const MdStarBorder = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.63-7.03L22 9.24z" />
  </svg>
);

export const FiEdit2 = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <path d="M17 3a2.828 2.828 0 114 4L7.5 20.5 2 22l1.5-5.5L17 3z" />
  </svg>
);

export const MdMarkEmailUnread = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M22 8.98V20c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V8.98l10 6 10-6zM20 4h-8c0 2.21 1.79 4 4 4s4-1.79 4-4zM12 11L4 6h16l-8 5z" />
  </svg>
);

export const MdCheck = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
  </svg>
);

export const MdReport = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M15.73 3H8.27L3 8.27v7.46L8.27 21h7.46L21 15.73V8.27L15.73 3zM12 17c-.55 0-1-.45-1-1s.45-1 1-1
     1 .45 1 1-.45 1-1 1zm1-4h-2V7h2v6z" />
  </svg>
);

export const MdRestoreFromTrash = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M6 21h12V7H6v14zm6-9l4 4h-3v4h-2v-4H8l4-4zM15.5 4l-1-1h-5l-1 1H5v2h14V4z" />
  </svg>
);

export const MdLabel = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M17.63 5.84C17.27 5.33 16.67 5 16 5H5c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 
     2h11c.67 0 1.27-.33 1.63-.84L22 12l-4.37-6.16z" />
  </svg>
);

export const MdArrowForward = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M12 4l1.41 1.41L7.83 11H20v2H7.83l5.58 5.59L12 20l-8-8 8-8z" />
  </svg>
);

export const MdArrowBack = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M20 11H7.83l5.58-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" />
  </svg>
);

export const MdMarkEmailRead = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M22 6.98V20c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6.98l10 6 10-6zM12 11L4 6h16l-8 5z" />
  </svg>
);
export const MdMarkEmailUnRead = ({ className = '', size = 20 }) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    className={className}
    width={size}
    height={size}
    fill="currentColor"
    viewBox="0 0 24 24"
  >
    <path d="M22 8.5V20a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8.5l10 6.5 10-6.5zm-10 4L2 6.5 12 2l10 4.5-10 6z"/>
  </svg>
);

export const MdMailOutline = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
    <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 
    2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z" />
  </svg>
);

export const FiArrowLeft = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <line x1="19" y1="12" x2="5" y2="12" />
    <polyline points="12 19 5 12 12 5" />
  </svg>
);

export const FiInbox = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <polyline points="22 12 16 12 14 15 10 15 8 12 2 12" />
    <path d="M5.45 5h13.1a2 2 0 012 2v10a2 2 0 01-2 2H5.45a2 2 0 01-2-2V7a2 2 0 012-2z" />
  </svg>
);

export const FiStar = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <polygon points="12 2 15 8.5 22 9.3 17 14.2 18.5 21 12 17.8 5.5 21 7 14.2 2 9.3 9 8.5 12 2" />
  </svg>
);

export const FiSend = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20"
    fill="none" stroke="currentColor" strokeWidth="2"
    strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <line x1="22" y1="2" x2="11" y2="13" />
    <polygon points="22 2 15 22 11 13 2 9 22 2" />
  </svg>
);

export const FiTrash2 = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20"
    fill="none" stroke="currentColor" strokeWidth="2"
    strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <polyline points="3 6 5 6 21 6" />
    <path d="M19 6l-2 14a2 2 0 01-2 2H9a2 2 0 01-2-2L5 6m5 0V4a2 2 0 012-2h0a2 2 0 012 2v2" />
  </svg>
);

export const FiFileText = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20"
    fill="none" stroke="currentColor" strokeWidth="2"
    strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
    <polyline points="14 2 14 8 20 8" />
    <line x1="16" y1="13" x2="8" y2="13" />
    <line x1="16" y1="17" x2="8" y2="17" />
    <polyline points="10 9 9 9 8 9" />
  </svg>
);

export const FiAlertCircle = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20"
    fill="none" stroke="currentColor" strokeWidth="2"
    strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <circle cx="12" cy="12" r="10" />
    <line x1="12" y1="8" x2="12" y2="12" />
    <line x1="12" y1="16" x2="12.01" y2="16" />
  </svg>
);

export const FiTag = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20"
    fill="none" stroke="currentColor" strokeWidth="2"
    strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <path d="M20 10V2h-8L4 10l10 10 6-6z" />
    <line x1="7" y1="7" x2="7.01" y2="7" />
  </svg>
);

export const FiMail = ({ className = '' }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" width="20" height="20"
    fill="none" stroke="currentColor" strokeWidth="2"
    strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24">
    <path d="M4 4h16v16H4z" />
    <polyline points="22,6 12,13 2,6" />
  </svg>
);
