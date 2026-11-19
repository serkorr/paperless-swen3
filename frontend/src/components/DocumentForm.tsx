import { useRef, useState, type SubmitEvent } from 'react'
import { uploadDocument } from '../services/documentService'
import { assignCategory, type Category } from '../services/categoryService'

type DocumentFormProps = {
    categories: Category[]
    onDocumentCreated: () => void
}

function DocumentForm({ categories, onDocumentCreated }: DocumentFormProps) {
    const [title, setTitle] = useState('')
    const [selectedFile, setSelectedFile] = useState<File | null>(null)
    const fileInputRef = useRef<HTMLInputElement>(null)
    const [categoryId, setCategoryId] = useState('')
    const [error, setError] = useState<string | null>(null)
    const [success, setSuccess] = useState<string | null>(null)
    const [saving, setSaving] = useState(false)

    async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()
        setError(null)
        setSuccess(null)

        // Validate the document title and selected PDF file.
        if (!title.trim()) {
            setError('Please enter a document title.')
            return
        }

        if (!selectedFile) {
            setError('Please select a PDF file.')
            return
        }

        // Allow only PDF files.
        if (
            selectedFile.type !== 'application/pdf' &&
            !selectedFile.name.toLowerCase().endsWith('.pdf')
        ) {
            setError('Only PDF files are allowed.')
            return
        }

        setSaving(true)

        try {
            // Upload the PDF to the backend, which stores it in MinIO.
            const createdDocument = await uploadDocument(
                selectedFile,
                title.trim()
            )

            // Assign the selected category to the uploaded document.
            if (categoryId) {
                try {
                    await assignCategory(createdDocument.id, Number(categoryId))
                } catch {
                    setError(
                        'PDF uploaded successfully, but the category could not be assigned.'
                    )
                }
            }

            // Reset the form after a successful upload.
            setTitle('')
            setSelectedFile(null)

            if (fileInputRef.current) {
                fileInputRef.current.value = ''
            }

            setCategoryId('')
            setSuccess('PDF uploaded successfully. OCR processing has been requested.')

            // Refresh the document list on the dashboard.
            onDocumentCreated()
        } catch {
            setError('Could not upload the PDF document. Please try again.')
        } finally {
            setSaving(false)
        }
    }

    return (
        <section>
            <h3>Upload New Document</h3>

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
                    <label htmlFor="pdfFile">Select PDF File</label>
                    <input
                        ref={fileInputRef}
                        id="pdfFile"
                        type="file"
                        accept=".pdf,application/pdf"
                        onChange={(event) => {
                            setSelectedFile(event.target.files?.[0] ?? null)
                        }}
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
                {success && <p role="status">{success}</p>}

                <button type="submit" disabled={saving}>
                    {saving ? 'Uploading...' : 'Upload PDF'}
                </button>
            </form>
        </section>
    )
}

export default DocumentForm
