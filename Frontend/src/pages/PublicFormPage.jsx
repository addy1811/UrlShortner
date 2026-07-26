import { useEffect, useState, useCallback } from 'react';
import { useParams } from 'react-router';
import * as formApi from '@/api/formApi';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';

export default function PublicFormPage() {
  const { linkId } = useParams();

  const [schema, setSchema] = useState(null);
  const [loadError, setLoadError] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  const [values, setValues] = useState({});
  const [submitError, setSubmitError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const loadSchema = useCallback(async () => {
    setIsLoading(true);
    setLoadError(null);
    try {
      const data = await formApi.getFormSchema(linkId);
      setSchema(data);
      // Seed empty values for every field so controlled inputs never start undefined.
      const initial = {};
      data.fields.forEach((f) => {
        initial[f.fieldKey] = '';
      });
      setValues(initial);
    } catch (err) {
      // Mirrors AccessControlService's exceptions: 403 (private/restricted/no access),
      // 404 (link not found), 410 (expired/exhausted) all surface as err.response.data.error.
      setLoadError(err.response?.data?.error || 'This form is unavailable.');
    } finally {
      setIsLoading(false);
    }
  }, [linkId]);

  useEffect(() => {
    loadSchema();
  }, [loadSchema]);

  function updateValue(fieldKey, value) {
    setValues((prev) => ({ ...prev, [fieldKey]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitError(null);
    setIsSubmitting(true);

    try {
      // Only send fields that actually have a value - matches how FormService
      // treats a missing key vs. an empty string for non-required fields.
      const responseData = Object.fromEntries(
        Object.entries(values).filter(([, v]) => v !== '')
      );
      await formApi.submitForm(linkId, responseData);
      setSubmitted(true);
    } catch (err) {
      // FormService throws ValidationException for missing-required /
      // invalid-option cases - GlobalExceptionHandler returns { error: "..." }.
      setSubmitError(err.response?.data?.error || 'Failed to submit the form. Please check your answers.');
    } finally {
      setIsSubmitting(false);
    }
  }

  if (isLoading) {
    return (
      <div className="mx-auto max-w-lg px-6 py-12">
        <p className="text-sm text-ink-muted">Loading…</p>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="mx-auto max-w-lg px-6 py-12 text-center">
        <h1 className="mb-2 text-xl">Can&rsquo;t open this form</h1>
        <p className="text-sm text-ink-muted">{loadError}</p>
      </div>
    );
  }

  if (submitted) {
    return (
      <div className="mx-auto max-w-lg px-6 py-12 text-center">
        <h1 className="mb-2 text-xl">Thanks!</h1>
        <p className="text-sm text-ink-muted">Your response has been submitted.</p>
      </div>
    );
  }

  if (schema.fields.length === 0) {
    return (
      <div className="mx-auto max-w-lg px-6 py-12 text-center">
        <p className="text-sm text-ink-muted">This link doesn&rsquo;t have a form set up.</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-lg px-6 py-12">
      <h1 className="mb-1 text-2xl">Fill out this form</h1>
      <p className="mb-8 text-sm text-ink-muted">Please complete the fields below.</p>

      <Card>
        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          {schema.fields.map((field) => (
            <FormFieldInput
              key={field.id}
              field={field}
              value={values[field.fieldKey]}
              onChange={(v) => updateValue(field.fieldKey, v)}
            />
          ))}

          {submitError && <p className="text-sm text-danger">{submitError}</p>}

          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Submitting…' : 'Submit'}
          </Button>
        </form>
      </Card>
    </div>
  );
}

function FormFieldInput({ field, value, onChange }) {
  const label = field.required ? `${field.label} *` : field.label;

  switch (field.fieldType) {
    case 'DROPDOWN':
      return (
        <div>
          <label className="mb-1 block text-sm font-medium">{label}</label>
          <select
            value={value}
            onChange={(e) => onChange(e.target.value)}
            required={field.required}
            className="w-full rounded-md border border-border px-3 py-2 outline-none focus:border-signal"
          >
            <option value="">Select…</option>
            {(field.options || []).map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        </div>
      );

    // CHECKBOX here is validated server-side as a single value against a fixed
    // option list (FormService.validateSubmission), so it's a radio group in
    // practice, not multi-select - matches how the backend actually checks it.
    case 'CHECKBOX':
      return (
        <div>
          <label className="mb-2 block text-sm font-medium">{label}</label>
          <div className="flex flex-col gap-2">
            {(field.options || []).map((opt) => (
              <label key={opt} className="flex items-center gap-2 text-sm">
                <input
                  type="radio"
                  name={field.fieldKey}
                  checked={value === opt}
                  onChange={() => onChange(opt)}
                  required={field.required}
                />
                {opt}
              </label>
            ))}
          </div>
        </div>
      );

    case 'NUMBER':
      return (
        <Input
          label={label}
          type="number"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          required={field.required}
        />
      );

    case 'DATE':
      return (
        <Input
          label={label}
          type="date"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          required={field.required}
        />
      );

    case 'EMAIL':
      return (
        <Input
          label={label}
          type="email"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          required={field.required}
        />
      );

    case 'TEXT':
    default:
      return (
        <Input
          label={label}
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          required={field.required}
        />
      );
  }
}