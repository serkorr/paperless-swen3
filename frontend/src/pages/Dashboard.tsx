import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { getDocuments, type Document } from '../services/documentService'
import { getCategories, type Category } from '../services/categoryService'
import DocumentForm from '../components/DocumentForm'
import CategoryForm from '../components/CategoryForm'

function Dashboard() {
    const [documents, setDocuments] = useState<Document[]>([])
    const [categories, setCategories] = useState<Category[]>([])
    const [loadingDocuments, setLoadingDocuments] = useState(true)
    const [loadingCategories, setLoadingCategories] = useState(true)
    const [documentError, setDocumentError] = useState<string | null>(null)
    const [categoryError, setCategoryError] = useState<string | null>(null)

    async function loadDocuments() {
        try {
            setDocumentError(null)
            const data = await getDocuments()
            setDocuments(data)
        } catch {
            setDocumentError('Could not load documents. Please try again.')
        } finally {
            setLoadingDocuments(false)
        }
    }

    async function loadCategories() {
        try {
            setCategoryError(null)
            const data = await getCategories()
            setCategories(data)
        } catch {
            setCategoryError('Could not load categories. Please try again.')
        } finally {
            setLoadingCategories(false)
        }
    }

    useEffect(() => {
        void loadDocuments()
        void loadCategories()
    }, [])

    return (
        <main>
            <h1>Paperless</h1>
            <h2>Document Dashboard</h2>
            <p>Manage your documents and categories in one place.</p>

            <DocumentForm
                categories={categories}
                onDocumentCreated={() => void loadDocuments()}
            />

            <CategoryForm onCategoryCreated={() => void loadCategories()} />

            <section>
                <h3>Categories</h3>

                {loadingCategories && <p>Loading categories...</p>}

                {categoryError && <p role="alert">{categoryError}</p>}

                {!loadingCategories && !categoryError && categories.length === 0 && (
                    <p>No categories found.</p>
                )}

                {!loadingCategories && !categoryError && categories.length > 0 && (
                    <ul>
                        {categories.map((category) => (
                            <li key={category.id}>
                                {category.name} (ID: {category.id})
                            </li>
                        ))}
                    </ul>
                )}
            </section>

            <section>
                <h3>Documents</h3>

                {loadingDocuments && <p>Loading documents...</p>}

                {documentError && <p role="alert">{documentError}</p>}

                {!loadingDocuments && !documentError && documents.length === 0 && (
                    <p>No documents found.</p>
                )}

                {!loadingDocuments && !documentError && documents.length > 0 && (
                    <table>
                        <thead>
                        <tr>
                            <th>Title</th>
                            <th>Filename</th>
                            <th>Status</th>
                            <th>Category</th>
                        </tr>
                        </thead>
                        <tbody>
                        {documents.map((document) => {
                            const category = categories.find(
                                (item) => item.id === document.categoryId
                            )

                            return (
                                <tr key={document.id}>
                                    <td>
                                        <Link to={`/documents/${document.id}`}>
                                            {document.title}
                                        </Link>
                                    </td>
                                    <td>{document.filename}</td>
                                    <td>{document.status}</td>
                                    <td>
                                        {category
                                            ? category.name.charAt(0).toUpperCase() + category.name.slice(1)
                                            : 'None'}
                                    </td>
                                </tr>
                            )
                        })}
                        </tbody>
                    </table>
                )}
            </section>
        </main>
    )
}

export default Dashboard