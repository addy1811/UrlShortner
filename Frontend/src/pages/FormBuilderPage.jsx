import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router';
import * as formApi from '@/api/formApi';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';

const FIELD_TYPES = ['TEXT', 'NUMBER', 'EMAIL', 'DATE', 'DROPDOWN', 'CHECKBOX'];

function emptyField() {
  return {
    fieldKey: '',
    label: '',
    fieldType: 'TEXT',
    required: false,
    optionsText: '',
  };
}

export default function FormBuilderPage() {
  const { linkId } = useParams();
  const navigate = useNavigate();

  const [fields, setFields] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState(null);
  const [saved, setSaved] = useState(false);

  const loadSchema = useCallback(async () => {
    setIsLoading(true);
    setLoadError(null);
    try {
      const schema = await formApi.getFormSchema(linkId);
      if (schema.fields.length > 0) {
        setFields(
          schema.fields
            .sort((a, b) => a.displayOrder - b.displayOrder)
            .map((f) => ({
              fieldKey: f.fieldKey,
              label: f.label,
              fieldType: f.fieldType,
              required: f.required,
              optionsText: (f.options || []).join(', '),
            }))
        );
      } else {
        setFields([emptyField()]);
      }
    } catch (err) {
      setLoadError(err.response?.data?.error || 'Failed to load the form schema');
    } finally {
      setIsLoading(false);
    }
  }, [linkId]);

  useEffect(() => {
    loadSchema();
  }, [loadSchema]);

  function updateField(index, patch) {
    setFields((prev) => prev.map((f, i) => (i === index ? { ...f, ...patch } : f)));
  }

  function addField() {
    setFields((prev) => [...prev, emptyField()]);
  }

  function removeField(index) {
    setFields((prev) => prev.filter((_, i) => i !== index));
  }

  function moveField(index, direction) {
    setFields((prev) => {
      const next = [...prev];
      const target = index + direction;
      if (target < 0 || target >= next.length) return prev;
      [next[index], next[target]] = [next[target], next[index]];
      return next;
    });
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaveError(null);
    setSaved(false);

    // Client-side mirror of FormService's server-side check, so the owner
    // gets immediate feedback instead of a round-trip 400.
    for (const f of fields) {
      const needsOptions = f.fieldType === 'DROPDOWN' || f.fieldType === 'CHECKBOX';
      if (needsOptions && !f.optionsText.trim()) {
        setSaveError(`Field "${f.fieldKey || f.label}" needs at least one option (comma-separated)`);
        return;
      }
    }

    setIsSaving(true);
    try {
      const payload = fields.map((f, index) => ({
        fieldKey: f.fieldKey,
        label: f.label,
        fieldType: f.fieldType,
        required: f.required,
        options:
          f.fieldType === 'DROPDOWN' || f.fieldType === 'CHECKBOX'
            ? f.optionsText.split(',').map((o) => o.trim()).filter(Boolean)
            : null,
        displayOrder: index,
      }));

      await formApi.defineFormFields(linkId, payload);
      setSaved(true);
    } catch (err) {
      const fieldErrors = err.response?.data?.fieldErrors;
      setSaveError(
        fieldErrors ? Object.values(fieldErrors)[0] : err.response?.data?.error || 'Failed to save the form'
      );
    } finally {
      setIsSaving(false);
    }
  }

  if (isLoading) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-12">
        <p className="text-sm text-ink-muted">Loading…</p>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="mx-auto max-w-3xl px-6 py-12">
        <p className="text-sm text-danger">{loadError}</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-6 py-12">
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl">Form builder</h1>
          <p className="mt-1 text-sm text-ink-muted">
            Define the fields visitors fill out through this link.
          </p>
        </div>
        <Button variant="secondary" onClick={() => navigate(`/links/${linkId}`)}>
          Back to link
        </Button>
      </div>

      <form onSubmit={handleSave} className="flex flex-col gap-4">
        {fields.map((field, index) => {
          const needsOptions = field.fieldType === 'DROPDOWN' || field.fieldType === 'CHECKBOX';
          return (
            <Card key={index} className="flex flex-col gap-4">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-ink-faint">Field {index + 1}</span>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => moveField(index, -1)}
                    disabled={index === 0}
                    className="text-xs text-ink-muted hover:text-ink disabled:opacity-30"
                  >
                    ↑ Up
                  </button>
                  <button
                    type="button"
                    onClick={() => moveField(index, 1)}
                    disabled={index === fields.length - 1}
                    className="text-xs text-ink-muted hover:text-ink disabled:opacity-30"
                  >
                    ↓ Down
                  </button>
                  <button
                    type="button"
                    onClick={() => removeField(index)}
                    className="text-xs text-danger hover:underline"
                  >
                    Remove
                  </button>
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <Input
                  label="Field key"
                  placeholder="phoneNumber"
                  value={field.fieldKey}
                  onChange={(e) => updateField(index, { fieldKey: e.target.value })}
                  required
                  hint="Letters/numbers/underscore, starts with a letter - this is the JSON key in submissions"
                />
                <Input
                  label="Label"
                  placeholder="Phone Number"
                  value={field.label}
                  onChange={(e) => updateField(index, { label: e.target.value })}
                  required
                />
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="mb-1 block text-sm font-medium">Field type</label>
                  <select
                    value={field.fieldType}
                    onChange={(e) => updateField(index, { fieldType: e.target.value })}
                    className="w-full rounded-md border border-border px-3 py-2 outline-none focus:border-signal"
                  >
                    {FIELD_TYPES.map((t) => (
                      <option key={t} value={t}>
                        {t}
                      </option>
                    ))}
                  </select>
                </div>

                <label className="flex items-center gap-2 self-end pb-2 text-sm">
                  <input
                    type="checkbox"
                    checked={field.required}
                    onChange={(e) => updateField(index, { required: e.target.checked })}
                  />
                  Required
                </label>
              </div>

              {needsOptions && (
                <Input
                  label="Options (comma-separated)"
                  placeholder="Small, Medium, Large"
                  value={field.optionsText}
                  onChange={(e) => updateField(index, { optionsText: e.target.value })}
                  required
                />
              )}
            </Card>
          );
        })}

        <Button type="button" variant="secondary" onClick={addField} className="self-start">
          + Add field
        </Button>

        {saveError && <p className="text-sm text-danger">{saveError}</p>}
        {saved && <p className="text-sm text-signal-dark">Form saved.</p>}

        <div className="mt-2 flex gap-3">
          <Button type="submit" disabled={isSaving}>
            {isSaving ? 'Saving…' : 'Save form'}
          </Button>
        </div>
      </form>
    </div>
  );
}