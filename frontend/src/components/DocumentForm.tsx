import { useState, type FormEvent } from 'react'
import { createDocument } from '../services/documentService'
import { assignCategory, type Category } from '../services/categoryService'

type DocumentFormProps = {
    categories: Category[]
    onDocumentCreated: () => void
}

function DocumentForm({ categories, onDocumentCreated }: DocumentFormProps) {
    const [title, setTitle] = useState('')
    const [filename, setFilename] = useState('')
    const [contentType, setContentType] = useState('application/pdf')
    const [categoryId, setCategoryId] = useState('')
    const [error, setError] = useState<string | null>(null)
    const [saving, setSaving] = useState(false)

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setError(null)

        // Validate document metadata before sending it to the backend.
        if (!title.trim() || !filename.trim() || !contentType.trim()) {
            setError('All fields are required.')
            return
        }

        setSaving(true)

        try {
            // Create the document and receive its generated ID.
            const createdDocument = await createDocument({
                title: title.trim(),
                filename: filename.trim(),
                contentType: contentType.trim(),
            })

            // Assign the selected category to the newly created document.
            if (categoryId) {
                await assignCategory(createdDocument.id, Number(categoryId))
            }

            setTitle('')
            setFilename('')
            setContentType('application/pdf')
            setCategoryId('')
            onDocumentCreated()
        } catch {
            setError(
                'Could not complete document creation. If a category was selected, the document may have been saved without it.'
            )
            onDocumentCreated()
        } finally {
            setSaving(false)
        }
    }

    return (
        <section>
            <h3>Add New Document</h3>

            <form onSubmit={(event) => void handleSubmit(event)}>
                <div>
                    <label htmlFor="title">Title</label>
                    <input
                        id="title"
                        value={title}
                        onChange={(event) => setTitle(event.target.value)}
                        required
                    />
                </div>

                <div>
                    <label htmlFor="filename">Filename</label>
                    <input
                        id="filename"
                        value={filename}
                        onChange={(event) => setFilename(event.target.value)}
                        required
                    />
                </div>

                <div>
                    <label htmlFor="contentType">Content Type</label>
                    <input
                        id="contentType"
                        value={contentType}
                        onChange={(event) => setContentType(event.target.value)}
                        required
                    />
                </div>

                <div>
                    <label htmlFor="category">Category (optional)</label>
                    <select
                        id="category"
                        value={categoryId}
                        onChange={(event) => setCategoryId(event.target.value)}
                    >
                        <option value="">No category</option>
                        {categories.map((category) => (
                            <option key={category.id} value={category.id}>
                                {category.name}
                            </option>
                        ))}
                    </select>
                </div>

                {error && <p role="alert">{error}</p>}

                <button type="submit" disabled={saving}>
                    {saving ? 'Saving...' : 'Create Document'}
                </button>
            </form>
        </section>
    )
}

export default DocumentForm