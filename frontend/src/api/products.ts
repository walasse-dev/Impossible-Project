export type Product = {
    id: number
    name: string
    description: string | null
    price: number
    stockQuantity: number
}

export type ProductPage = {
    content: Product[]
    page: number
    size: number
    totalElements: number
    totalPages: number
}

type ListProductsOptions = {
    search: string
    page: number
    size: number
    signal?: AbortSignal
}

const productsUrl = '/api/v1/products'

export async function listProducts({
                                       search,
                                       page,
                                       size,
                                       signal,
                                   }: ListProductsOptions): Promise<ProductPage> {
    const params = new URLSearchParams({
        page: String(page),
        size: String(size),
    })

    if (search.trim()) {
        params.set('search', search.trim())
    }

    const response = await fetch(productsUrl + '?' + params.toString(), { signal })

    if (!response.ok) {
        throw new Error('Falha ao carregar o catálogo.')
    }

    return response.json()
}

export async function getProduct(id: string, signal?: AbortSignal): Promise<Product | null> {
    const response = await fetch(productsUrl + '/' + encodeURIComponent(id), { signal })

    if (response.status === 404) {
        return null
    }

    if (!response.ok) {
        throw new Error('Falha ao carregar o produto.')
    }

    return response.json()
}