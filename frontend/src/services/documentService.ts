// Represents a document returned by the Spring Boot REST API.
export type Document = {
    id: number
    title: string
    filename: string
    contentType: string
    uploadedAt: string
    status: string
    categoryId: number | null
}

// Retrieves all documents from the backend.
export async function getDocuments(): Promise<Document[]> {
    const response = await fetch('/api/documents')

    if (!response.ok) {
        throw new Error('Failed to fetch documents')
    }

    return response.json() as Promise<Document[]>
}

// Retrieves a single document using its ID.
export async function getDocumentById(id: number): Promise<Document> {
    const response = await fetch(`/api/documents/${id}`)

    if (!response.ok) {
        throw new Error(`Failed to fetch document with ID ${id}`)
    }

    return response.json() as Promise<Document>
}

export type CreateDocumentRequest = {
    title: string
    filename: string
    contentType: string
}

// Sends document metadata to the Spring Boot REST API.
export async function createDocument(
    document: CreateDocumentRequest
): Promise<Document> {
    const response = await fetch('/api/documents', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(document),
    })

    if (!response.ok) {
        throw new Error('Failed to create document')
    }

    return response.json() as Promise<Document>
}

export type UpdateDocumentRequest = {
    title: string
    filename: string
    contentType: string
}

// Updates the metadata of an existing document.
export async function updateDocument(
    id: number,
    document: UpdateDocumentRequest
): Promise<Document> {
    const response = await fetch(`/api/documents/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(document),
    })

    if (!response.ok) {
        throw new Error('Failed to update document')
    }

    return response.json() as Promise<Document>
}

// Deletes a document using its ID.
export async function deleteDocument(id: number): Promise<void> {
    const response = await fetch(`/api/documents/${id}`, {
        method: 'DELETE',
    })

    if (!response.ok) {
        throw new Error('Failed to delete document')
    }
}