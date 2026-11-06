import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { getProduct } from '../api/products'
import type { Product } from '../api/products'
import { formatPrice } from '../formatPrice'

type DetailResult =
    | { id: string; status: 'ready'; product: Product }
    | { id: string; status: 'not-found' }
    | { id: string; status: 'error' }

export default function ProductDetailPage() {
    const { id } = useParams()
    const [result, setResult] = useState<DetailResult | null>(null)
    const detail = result?.id === id ? result : null

    useEffect(() => {
        if (!id) {
            return
        }

        const controller = new AbortController()

        getProduct(id, controller.signal)
            .then((product) => {
                if (controller.signal.aborted) {
                    return
                }

                setResult(product ? { id, status: 'ready', product } : { id, status: 'not-found' })
            })
            .catch(() => {
                if (!controller.signal.aborted) {
                    setResult({ id, status: 'error' })
                }
            })

        return () => controller.abort()
    }, [id])

    return (
        <main>
            <Link to="/">Voltar ao catálogo</Link>

            {!detail && <p role="status">Carregando produto…</p>}
            {detail?.status === 'error' && <p role="alert">Não foi possível carregar o produto.</p>}
            {detail?.status === 'not-found' && <h1>Produto não encontrado</h1>}

            {detail?.status === 'ready' && (
                <>
                    <h1>{detail.product.name}</h1>
                    <p>{detail.product.description || 'Produto sem descrição.'}</p>
                    <p>{formatPrice(detail.product.price)}</p>
                    <p>Unidades em estoque: {detail.product.stockQuantity}</p>
                </>
            )}
        </main>
    )
}