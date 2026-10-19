import { useState, type FormEvent } from 'react'
import { createCategory } from '../services/categoryService'

type CategoryFormProps = {
    onCategoryCreated: () => void
}

function CategoryForm({ onCategoryCreated }: CategoryFormProps) {
    const [name, setName] = useState('')
    const [error, setError] = useState<string | null>(null)
    const [saving, setSaving] = useState(false)

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        setError(null)

        // Validate the category name before sending it to the backend.
        const trimmedName = name.trim()

        if (!trimmedName) {
            setError('Category name is required.')
            return
        }

        if (trimmedName.length > 100) {
            setError('Category name must not exceed 100 characters.')
            return
        }

        setSaving(true)

        try {
            await createCategory(trimmedName)
            setName('')
            onCategoryCreated()
        } catch {
            setError('Could not create category. The name may already exist.')
        } finally {
            setSaving(false)
        }
    }

    return (
        <section>
            <h3>Add New Category</h3>

            <form onSubmit={(event) => void handleSubmit(event)}>
                <div>
                    <label htmlFor="categoryName">Category Name</label>
                    <input
                        id="categoryName"
                        type="text"
                        value={name}
                        onChange={(event) => setName(event.target.value)}
                        maxLength={100}
                        required
                    />
                </div>

                {error && <p role="alert">{error}</p>}

                <button type="submit" disabled={saving}>
                    {saving ? 'Saving...' : 'Create Category'}
                </button>
            </form>
        </section>
    )
}

export default CategoryForm