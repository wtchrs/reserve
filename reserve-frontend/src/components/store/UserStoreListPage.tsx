import {Box, Button, Typography} from '@mui/material'
import {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import storeService from '../../services/storeService.ts'
import type {PageParams, Store} from '../../../types/domain.d.ts'
import PageNavigator from '../PageNavigator.tsx'
import StoreList from './StoreList.tsx'

function UserStoreListPage() {
    const {username} = useParams<{ username: string }>()
    const navigate = useNavigate()

    const [stores, setStores] = useState<Store[]>([])
    const [pageParam, setPageParam] = useState<PageParams<Store>>({} as PageParams<Store>)
    const [hasNext, setHasNext] = useState(false)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState()

    useEffect(() => {
        if (!username) return
        const newPageParam = {...pageParam, page: 0}
        setLoading(true)
        storeService.getStoresByUsername(username, newPageParam)
            .then(res => {
                setStores(res.results)
                setHasNext(res.hasNext)
                setLoading(false)
                setPageParam(newPageParam)
            })
            .catch(err => setError(err))
    }, [username])

    if (error) throw error
    if (!username) throw new Response('Resource Not Found', {status: 404})

    const onPageMove = async (move: number) => {
        const newPage = {
            ...pageParam,
            page: Math.max(0, pageParam.page + move),
        }
        setLoading(true)
        const res = await storeService.getStoresByUsername(username, newPage)
        setStores(res.results)
        setHasNext(res.hasNext)
        setPageParam(newPage)
        setLoading(false)
    }

    return (
        <Box sx={{mb: 4}}>
            <Button variant="text" onClick={() => navigate(-1)} sx={{mb: 3, textTransform: 'none'}}>
                {'< Go Back'}
            </Button>

            <Typography variant="h4" align="center" gutterBottom>
                {username}'s Stores
            </Typography>

            {stores.length > 0 && (
                <Box sx={{mt: 4}}>
                    <Typography variant="h6" gutterBottom>
                        Search Results
                    </Typography>
                    <StoreList stores={stores}/>
                </Box>
            )}

            {!loading && stores.length === 0 && (
                <Typography align="center" sx={{mt: 2}}>
                    No results found
                </Typography>
            )}

            <PageNavigator hasPrevious={pageParam.page > 0} hasNext={hasNext} onPageMove={onPageMove}/>
        </Box>
    )
}

export default UserStoreListPage
