export type Category = {
    id: number
    name: string
}

// Retrieves all categories from the Spring Boot REST API.
export async function getCategories(): Promise<Category[]> {
    const response = await fetch('/api/categories')

    if (!response.ok) {
        throw new Error('Failed to fetch categories')
    }

    return response.json() as Promise<Category[]>
}

// Creates a new category in the backend.
export async function createCategory(name: string): Promise<Category> {
    const response = await fetch('/api/categories', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name }),
    })

    if (!response.ok) {
        throw new Error('Failed to create category')
    }

    return response.json() as Promise<Category>
}

// Assigns an existing category to a document.
export async function assignCategory(
    documentId: number,
    categoryId: number
): Promise<void> {
    const response = await fetch(
        `/api/documents/${documentId}/category/${categoryId}`,
        { method: 'PUT' }
    )

    if (!response.ok) {
        throw new Error('Failed to assign category')
    }
}