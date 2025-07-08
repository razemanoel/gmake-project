import { updateMailLabel, deleteMail, getMailById } from '../services/mailService';

export async function toggleStar(mail, token, logout, onUpdated) {
  const currentLabelIds = mail.labels?.map(l => (typeof l === 'string' ? l : l.id)) || [];
  const isStarred = mail.labels?.some(label => label.name?.toLowerCase() === 'star');

  const updatedLabels = isStarred
    ? currentLabelIds.filter(id => id !== 'star')
    : [...new Set([...currentLabelIds, 'star'])];

  await updateMailLabel(token, mail.id, updatedLabels, logout);
  const refreshed = await getMailById(token, mail.id, logout);
  if (onUpdated) onUpdated(refreshed);
}

export async function toggleRead(mail, token, logout, onUpdated) {
  const currentLabelIds = mail.labels?.map(l => (typeof l === 'string' ? l : l.id)) || [];
  const isRead = mail.labels?.some(label => label.name?.toLowerCase() === 'read');

  const updatedLabels = isRead
    ? [...new Set([...currentLabelIds.filter(id => id !== 'read'), 'unread'])]
    : [...new Set([...currentLabelIds.filter(id => id !== 'unread'), 'read'])];

  await updateMailLabel(token, mail.id, updatedLabels, logout);
  const refreshed = await getMailById(token, mail.id, logout);
  if (onUpdated) onUpdated(refreshed);
}

export async function trashOrDelete(mail, token, logout, onDeleted, isTrashView = false) {
  const isTrash = mail.labels?.some(label => label.name?.toLowerCase() === 'trash');

  if (isTrash || isTrashView) {
    await deleteMail(token, mail.id, logout);
    if (onDeleted) onDeleted(mail.id);
    return;
  }

  const currentLabelIds = mail.labels?.map(l => (typeof l === 'string' ? l : l.id)) || [];
  const updatedLabels = [...currentLabelIds.filter(id => id !== 'received' && id !== 'sent'), 'trash'];

  await updateMailLabel(token, mail.id, updatedLabels, logout);
  const refreshed = await getMailById(token, mail.id, logout);
  if (onDeleted) onDeleted(mail.id); 
}

export async function restoreFromTrash(mail, user, token, logout, onUpdated) {
  const currentLabelIds = mail.labels?.map(l => (typeof l === 'string' ? l : l.id)) || [];
  const filtered = currentLabelIds.filter(l => l !== 'trash');

  const isSender = mail.fromUserId === user?.id;
  const isRecipient = mail.toUserId === user?.id;
  const isSelfMail = isSender && isRecipient;

  let restored;
  if (isSelfMail) {
    restored = [...new Set([...filtered, 'sent', 'received', 'read'])];
  } else if (isSender) {
    restored = [...new Set([...filtered, 'sent', 'read'])];
  } else {
    restored = [...new Set([...filtered, 'received', 'read'])];
  }

  await updateMailLabel(token, mail.id, restored, logout);
  const refreshed = await getMailById(token, mail.id, logout);
  if (onUpdated) onUpdated(refreshed);
}
