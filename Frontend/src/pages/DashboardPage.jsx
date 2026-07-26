import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchMyLinks, createLinkThunk, deleteLinkThunk } from '@/store/linksSlice';
import CreateLinkForm from '@/components/links/CreateLinkForm';
import LinkCard from '@/components/links/LinkCard';
import Card from '@/components/ui/Card';

export default function DashboardPage() {
  const dispatch = useDispatch();
  const { items, status, error, totalElements } = useSelector((state) => state.links);
  const [showCreateForm, setShowCreateForm] = useState(false);

  useEffect(() => {
    dispatch(fetchMyLinks({ page: 0, size: 20 }));
  }, [dispatch]);

  async function handleCreate(payload) {
    await dispatch(createLinkThunk(payload)).unwrap();
    setShowCreateForm(false);
  }

  async function handleDelete(linkId) {
    if (!window.confirm('Delete this link permanently? This cannot be undone.')) {
      return;
    }
    try {
      await dispatch(deleteLinkThunk(linkId)).unwrap();
    } catch {
      window.alert('Failed to delete the link. Please try again.');
    }
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-12">
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl">Your links</h1>
          <p className="mt-1 text-sm text-ink-muted">
            {totalElements} link{totalElements === 1 ? '' : 's'}
          </p>
        </div>
        <button
          onClick={() => setShowCreateForm((v) => !v)}
          className="rounded-md bg-signal px-4 py-2 text-sm font-medium text-white hover:bg-signal-dark"
        >
          {showCreateForm ? 'Cancel' : 'New link'}
        </button>
      </div>

      {showCreateForm && (
        <Card className="mb-8">
          <CreateLinkForm onCreate={handleCreate} />
        </Card>
      )}

      {status === 'loading' && (
        <p className="text-sm text-ink-muted">Loading your links…</p>
      )}

      {status === 'failed' && (
        <p className="text-sm text-danger">Failed to load links: {error}</p>
      )}

      {status === 'succeeded' && items.length === 0 && (
        <Card className="text-center text-sm text-ink-muted">
          No links yet. Create your first one above.
        </Card>
      )}

      <div className="flex flex-col gap-3">
        {items.map((link) => (
          <LinkCard key={link.id} link={link} onDelete={handleDelete} />
        ))}
      </div>
    </div>
  );
}