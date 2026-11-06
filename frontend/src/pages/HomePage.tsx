import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router'
import { listProducts } from '../api/products'
import type { ProductPage } from '../api/products'
import { formatPrice } from '../formatPrice'

const pageSize = 20

type CatalogState =
    | { status: 'loading' }
    | { status: 'ready'; data: ProductPage }
    | { status: 'error' }

export default function HomePage() {
    const [searchInput, setSearchInput] = useState('')
    const [search, setSearch] = useState('')
    const [page, setPage] = useState(0)
    const [catalog, setCatalog] = useState<CatalogState>({ status: 'loading' })

    useEffect(() => {
        const controller = new AbortController()

        listProducts({ search, page, size: pageSize, signal: controller.signal })
            .then((data) => {
                if (!controller.signal.aborted) {
                    setCatalog({ status: 'ready', data })
                }
            })
            .catch(() => {
                if (!controller.signal.aborted) {
                    setCatalog({ status: 'error' })
                }
            })

        return () => controller.abort()
    }, [search, page])

    function handleSearch(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        const nextSearch = searchInput.trim()

        if (nextSearch === search && page === 0) {
            return
        }

        setCatalog({ status: 'loading' })
        setSearch(nextSearch)
        setPage(0)
    }

    function changePage(nextPage: number) {
        setCatalog({ status: 'loading' })
        setPage(nextPage)
    }

    return (
        <main>
            <h1>Impossible Project</h1>
            <h2>Catálogo de produtos</h2>

            <form onSubmit={handleSearch}>
                <label htmlFor="product-search">Buscar por nome</label>{' '}
                <input
                    id="product-search"
                    type="search"
                    value={searchInput}
                    onChange={(event) => setSearchInput(event.target.value)}
                />{' '}
                <button type="submit">Buscar</button>
            </form>

            {catalog.status === 'loading' && <p role="status">Carregando produtos…</p>}
            {catalog.status === 'error' && (
                <p role="alert">Não foi possível carregar os produtos.</p>
            )}

            {catalog.status === 'ready' && (
                <>
                    {catalog.data.content.length === 0 ? (
                        <p>Nenhum produto encontrado.</p>
                    ) : (
                        <ul>
                            {catalog.data.content.map((product) => (
                                <li key={product.id}>
                                    <h3>
                                        <Link to={'/products/' + product.id}>{product.name}</Link>
                                    </h3>
                                    <p>{formatPrice(product.price)}</p>
                                    <p>{product.stockQuantity > 0 ? 'Em estoque' : 'Sem estoque'}</p>
                                </li>
                            ))}
                        </ul>
                    )}

                    {(page > 0 || catalog.data.totalPages > 1) && (
                        <nav aria-label="Paginação do catálogo">
                            <button type="button" disabled={page === 0} onClick={() => changePage(page - 1)}>
                                Anterior
                            </button>{' '}
                            <span>Página {page + 1}</span>{' '}
                            <button
                                type="button"
                                disabled={page + 1 >= catalog.data.totalPages}
                                onClick={() => changePage(page + 1)}
                            >
                                Próxima
                            </button>
                        </nav>
                    )}
                </>
            )}
        </main>
    )
}