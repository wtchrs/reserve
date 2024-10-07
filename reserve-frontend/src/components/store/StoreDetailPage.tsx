import {Box, Button} from '@mui/material'
import {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import storeService from '../../services/storeService.ts'
import {Store} from '../../type.ts'
import StoreDetail from './StoreDetail.tsx'

function StoreDetailPage() {
    const {storeId} = useParams()
    const navigate = useNavigate()
    const [store, setStore] = useState<Store>()
    const [error, setError] = useState()

    useEffect(() => {
        if (!storeId) throw new Response('Resource Not Found', {status: 404})
        storeService.getStore(storeId)
            .then(res => setStore(res))
            .catch(err => {
                console.log('err', err)
                setError(err)
            })
    }, [storeId])

    if (error) throw error

    return (
        <Box sx={{mb: 4}}>
            <Button variant="text" onClick={() => navigate(-1)} sx={{marginBottom: 3}}>
                {'< Go Back'}
            </Button>
            <Box sx={{mx: 5}}>
                <StoreDetail store={store}/>
            </Box>
        </Box>
    )
}

export default StoreDetailPage
