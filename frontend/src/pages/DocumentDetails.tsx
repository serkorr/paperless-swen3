import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import {
    getDocumentById,
    updateDocument,
    deleteDocument,
    type Document,
} from '../services/documentService'
import {
    assignCategory,
    getCategories,
    type Category,
} from '../services/categoryService'

function DocumentDetails() {
    const { id } = useParams<{ id: string }>()
    const navigate = useNavigate()

    const [document, setDocument] = useState<Document | null>(null)
    const [categories, setCategories] = useState<Category[]>([])
    const [selectedCategoryId, setSelectedCategoryId] = useState('')

    const [title, setTitle] = useState('')
    const [filename, setFilename] = useState('')
    const [contentType, setContentType] = useState('')

    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [deleting, setDeleting] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [success, setSuccess] = useState<string | null>(null)

    useEffect(() => {
        async function loadData() {
            setLoading(true)
            setError(null)

            if (!id || !/^[1-9]\d*$/.test(id)) {
                setError('Invalid document ID.')
                setLoading(false)
                return
            }

            try {
                const [documentData, categoryData] = await Promise.all([
                    getDocumentById(Number(id)),
                    getCategories(),
                ])

                setDocument(documentData)
                setCategories(categoryData)
                setSelectedCategoryId(documentData.categoryId?.toString() ?? '')

                setTitle(documentData.title)
                setFilename(documentData.filename)
                setContentType(documentData.contentType)
            } catch {
                setError('Could not load document details.')
            } finally {
                setLoading(false)
            }
        }

        void loadData()
    }, [id])

    async function handleUpdate(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        if (!document || saving) return

        setError(null)
        setSuccess(null)

        // Validate document metadata before updating it.
        if (!title.trim() || !filename.trim() || !contentType.trim()) {
            setError('All fields are required.')
            return
        }

        setSaving(true)

        try {
            // Update the document metadata using the REST API.
            const updatedDocument = await updateDocument(document.id, {
                title: title.trim(),
                filename: filename.trim(),
                contentType: contentType.trim(),
            })

            setDocument(updatedDocument)
            setTitle(updatedDocument.title)
            setFilename(updatedDocument.filename)
            setContentType(updatedDocument.contentType)
            setSuccess('Document updated successfully.')
        } catch {
            setError('Could not update the document.')
        } finally {
            setSaving(false)
        }
    }

    async function handleCategoryChange() {
        if (!document || saving || !selectedCategoryId) {
            setError('Please select a category.')
            return
        }

        setSaving(true)
        setError(null)
        setSuccess(null)

        try {
            // Assign the category and load the updated document.
            await assignCategory(document.id, Number(selectedCategoryId))
            const updatedDocument = await getDocumentById(document.id)

            setDocument(updatedDocument)
            setSuccess('Category updated successfully.')
        } catch {
            setError('Could not update the category.')
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete() {
        if (!document || deleting) return

        // Require confirmation before permanently deleting a document.
        const confirmed = window.confirm(
            `Are you sure you want to delete "${document.title}"?`
        )

        if (!confirmed) return

        setDeleting(true)
        setError(null)
        setSuccess(null)

        try {
            await deleteDocument(document.id)
            navigate('/')
        } catch {
            setError('Could not delete the document.')
            setDeleting(false)
        }
    }

    const currentCategory = categories.find(
        (category) => category.id === document?.categoryId
    )

    return (
        <main>
            <Link to="/">← Back to Dashboard</Link>

            <h1>Document Details</h1>

            {loading && <p>Loading document...</p>}

            {error && <p role="alert">{error}</p>}
            {success && <p role="status">{success}</p>}

            {!loading && document && (
                <>
                    <section>
                        <h2>{document.title}</h2>

                        <p><strong>ID:</strong> {document.id}</p>
                        <p><strong>Status:</strong> {document.status}</p>
                        <p>
                            <strong>Uploaded At:</strong>{' '}
                            {new Date(document.uploadedAt).toLocaleString()}
                        </p>
                        <p>
                            <strong>Category:</strong>{' '}
                            {currentCategory
                                ? currentCategory.name.charAt(0).toUpperCase() +
                                currentCategory.name.slice(1)
                                : 'None'}
                        </p>
                    </section>

                    <section>
                        <h3>Edit Document</h3>

                        <form onSubmit={(event) => void handleUpdate(event)}>
                            <div>
                                <label htmlFor="editTitle">Title</label>
                                <input
                                    id="editTitle"
                                    value={title}
                                    onChange={(event) => setTitle(event.target.value)}
                                    required
                                />
                            </div>

                            <div>
                                <label htmlFor="editFilename">Filename</label>
                                <input
                                    id="editFilename"
                                    value={filename}
                                    onChange={(event) => setFilename(event.target.value)}
                                    required
                                />
                            </div>

                            <div>
                                <label htmlFor="editContentType">Content Type</label>
                                <input
                                    id="editContentType"
                                    value={contentType}
                                    onChange={(event) => setContentType(event.target.value)}
                                    required
                                />
                            </div>

                            <button type="submit" disabled={saving || deleting}>
                                {saving ? 'Saving...' : 'Save Changes'}
                            </button>
                        </form>
                    </section>

                    <section>
                        <h3>Change Category</h3>

                        <div>
                            <label htmlFor="documentCategory">Select Category</label>
                            <select
                                id="documentCategory"
                                value={selectedCategoryId}
                                onChange={(event) => setSelectedCategoryId(event.target.value)}
                            >
                                <option value="">Select a category</option>
                                {categories.map((category) => (
                                    <option key={category.id} value={category.id}>
                                        {category.name.charAt(0).toUpperCase() +
                                            category.name.slice(1)}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <button
                            type="button"
                            onClick={() => void handleCategoryChange()}
                            disabled={saving || deleting || !selectedCategoryId}
                        >
                            {saving ? 'Saving...' : 'Update Category'}
                        </button>
                    </section>

                    <section>
                        <h3>Delete Document</h3>

                        <p>This action permanently deletes the document record.</p>

                        <button
                            type="button"
                            onClick={() => void handleDelete()}
                            disabled={saving || deleting}
                        >
                            {deleting ? 'Deleting...' : 'Delete Document'}
                        </button>
                    </section>
                </>
            )}
        </main>
    )
}

export default DocumentDetails