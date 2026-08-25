import { useEffect, useState, useCallback } from 'react';
import { useParams, Link } from 'react-router';
import * as linksApi from '@/api/linksApi';
import * as grantsApi from '@/api/grantsApi';
import * as formApi from '@/api/formApi';
import Card from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import GrantAccessModal from '@/components/links/GrantAccessModal';

export default function LinkDetailPage() {
  const { linkId } = useParams();

  const [link, setLink] = useState(null);
  const [loadError, setLoadError] = useState(null);

  const [editForm, setEditForm] = useState(null);
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);

  const [grants, setGrants] = useState([]);
  const [showGrantModal, setShowGrantModal] = useState(false);

  const [responses, setResponses] = useState(null); // null = not loaded yet

  const loadLink = useCallback(async () => {
    try {
      const data = await linksApi.getLink(linkId);
      setLink(data);
      setEditForm({
        visibility: data.visibility,
        active: data.active,
        maxUses: data.maxUses ?? '',
        expiresAt: data.expiresAt ? data.expiresAt.slice(0, 16) : '',
      });
    } catch (err) {
      setLoadError(err.response?.data?.error || 'Failed to load this link');
    }
  }, [linkId]);

  const loadGrants = useCallback(async () => {
    try {
      const data = await grantsApi.listGrants(linkId);
      setGrants(data);
    } catch {
    }
  }, [linkId]);

  useEffect(() => {
    loadLink();
  }, [loadLink]);

  useEffect(() => {
    if (link?.visibility === 'RESTRICTED') {
      loadGrants();
    }
  }, [link?.visibility, loadGrants]);

  async function handleSave(e) {
    e.preventDefault();
    setSaveError(null);
    setIsSaving(true);
    try {
      const updated = await linksApi.updateLink(linkId, {
        visibility: editForm.visibility,
        active: editForm.active,
        maxUses: editForm.maxUses === '' ? null : Number(editForm.maxUses),
        expiresAt: editForm.expiresAt ? new Date(editForm.expiresAt).toISOString() : null,
      });
      setLink(updated);
    } catch (err) {
      setSaveError(err.response?.data?.error || 'Failed to save changes');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleGrant({ username, email }) {
    await grantsApi.grantAccess(linkId, { username, email });
    await loadGrants();
  }

  async function handleRevoke(grantId) {
  try {
    await grantsApi.revokeAccess(linkId, grantId);
    setGrants((prev) =>
      prev.map((g) => (g.id === grantId ? { ...g, status: 'REVOKED' } : g))
    );
  } catch (err) {
    window.alert(err.response?.data?.error || 'Failed to revoke access. Please try again.');
  }
}

async function handleReactivate(grantId) {
  try {
    const updated = await grantsApi.reactivateAccess(linkId, grantId);
    setGrants((prev) => prev.map((g) => (g.id === grantId ? updated : g)));
  } catch (err) {
    window.alert(err.response?.data?.error || 'Failed to reactivate access. Please try again.');
  }
}
  async function handleLoadResponses() {
    try {
      const data = await formApi.getFormResponses(linkId);
      setResponses(data);
    } catch {
      setResponses({ content: [], totalElements: 0 });
    }
  }

  if (loadError) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-12">
        <p className="text-sm text-danger">{loadError}</p>
      </div>
    );
  }

  if (!link || !editForm) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-12">
        <p className="text-sm text-ink-muted">Loading…</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-12">
      <div className="mb-6 flex items-center gap-3">
        <h1 className="font-mono-code text-2xl">{link.shortCode}</h1>
        <Badge visibility={link.visibility} />
      </div>
      <p className="mb-8 break-all text-sm text-ink-muted">{link.shortUrl}</p>

      {/* ===== Edit settings ===== */}
      <Card className="mb-6">
        <h2 className="mb-4 text-lg">Settings</h2>
        <form onSubmit={handleSave} className="flex flex-col gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium">Visibility</label>
            <select
              value={editForm.visibility}
              onChange={(e) => setEditForm((f) => ({ ...f, visibility: e.target.value }))}
              className="w-full rounded-md border border-border px-3 py-2 outline-none focus:border-signal"
            >
              <option value="PRIVATE">Private — only you</option>
              <option value="PUBLIC">Public — anyone with the link</option>
              <option value="RESTRICTED">Restricted — only people you grant access to</option>
            </select>
          </div>

          <Input
            label="Max uses"
            type="number"
            min="1"
            placeholder="Unlimited"
            value={editForm.maxUses}
            onChange={(e) => setEditForm((f) => ({ ...f, maxUses: e.target.value }))}
          />

          <Input
            label="Expires at"
            type="datetime-local"
            value={editForm.expiresAt}
            onChange={(e) => setEditForm((f) => ({ ...f, expiresAt: e.target.value }))}
          />

          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={editForm.active}
              onChange={(e) => setEditForm((f) => ({ ...f, active: e.target.checked }))}
            />
            Active
          </label>

          {saveError && <p className="text-sm text-danger">{saveError}</p>}

          <Button type="submit" disabled={isSaving} className="self-start">
            {isSaving ? 'Saving…' : 'Save changes'}
          </Button>
        </form>
      </Card>

      {/* ===== Grants - only relevant for RESTRICTED links ===== */}
      {link.visibility === 'RESTRICTED' && (
        <Card className="mb-6">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg">Who has access</h2>
            <Button variant="secondary" onClick={() => setShowGrantModal(true)}>
              Grant access
            </Button>
          </div>

          {grants.length === 0 ? (
            <p className="text-sm text-ink-muted">No one has been granted access yet.</p>
          ) : (
            <ul className="flex flex-col gap-2">
              {grants.map((grant) => (
                <li key={grant.id} className="flex items-center justify-between text-sm">
                   <span>
                       {grant.granteeUsername || grant.invitedEmail}
                   <span className="ml-2 text-xs text-ink-faint">
                        {grant.status === 'PENDING' ? '(invited, not yet registered)' : grant.status}
                   </span>
                    </span>

                          {grant.status === 'ACTIVE' && (
                    <button onClick={() => handleRevoke(grant.id)} className="text-xs text-danger hover:underline">
                                 Revoke
                               </button>
                                 )}
                          {grant.status === 'REVOKED' && (
                       <button onClick={() => handleReactivate(grant.id)} className="text-xs text-signal hover:underline">
                           Grant again
                             </button>
                                 )}
           
                 </li>
              ))}
            </ul>
          )}

          {showGrantModal && (
            <GrantAccessModal onGrant={handleGrant} onClose={() => setShowGrantModal(false)} />
          )}
        </Card>
      )}

      {/* ===== Form + responses ===== */}
      <Card>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg">Data collection form</h2>
          <Link
            to={`/links/${linkId}/form-builder`}
            className="rounded-md border border-border px-3 py-1.5 text-sm text-ink-muted hover:border-ink-faint hover:text-ink"
          >
            Edit form fields
          </Link>
        </div>

        {responses === null ? (
          <button onClick={handleLoadResponses} className="text-sm text-signal hover:underline">
            Load responses
          </button>
        ) : responses.content.length === 0 ? (
          <p className="text-sm text-ink-muted">No responses submitted yet.</p>
        ) : (
          <div className="flex flex-col gap-3">
            <p className="text-sm text-ink-muted">{responses.totalElements} response(s)</p>
            {responses.content.map((r) => (
              <div key={r.id} className="rounded-md border border-border p-3 text-sm">
                <p className="mb-1 text-xs text-ink-faint">
                  {new Date(r.submittedAt).toLocaleString()}
                </p>
                {Object.entries(r.responseData).map(([key, value]) => (
                  <p key={key}>
                    <span className="text-ink-muted">{key}:</span> {String(value)}
                  </p>
                ))}
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}