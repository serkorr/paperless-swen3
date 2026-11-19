export interface SearchResult {
    documentId: number
    title: string
    filename: string
    extractedText: string
}

// Search documents by their OCR text.
export async function searchDocuments(query: string): Promise<SearchResult[]> {
    const response = await fetch(
        `/api/search?query=${encodeURIComponent(query)}`
    )

    if (!response.ok) {
        throw new Error('Failed to search documents')
    }

    return response.json() as Promise<SearchResult[]>
}
