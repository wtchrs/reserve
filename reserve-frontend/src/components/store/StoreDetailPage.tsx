import {Box, Button} from '@mui/material'
import {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {useAuth} from '../../hooks/useAuth.tsx'
import storeService from '../../services/storeService.ts'
import {Store} from '../../type.ts'
import MenuList from './menu/MenuList.tsx'
import StoreDetail from './StoreDetail.tsx'

function isInteger(value?: string) {
    return value != undefined && /^\d+$/.test(value)
}

function StoreDetailPage() {
    const {auth} = useAuth()
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

    if (!storeId || !isInteger(storeId)) throw new Response('Resource Not Found', {status: 404})
    if (error) throw error

    return (
        <Box sx={{mb: 4}}>
            <Box sx={{mb: 3, display: 'flex', flexDirection: 'row', justifyContent: 'space-between'}}>
                <Button variant="text" sx={{textTransform: 'none'}} onClick={() => navigate(-1)}>
                    {'< Go Back'}
                </Button>
                {auth?.user.username === store?.registrant && (
                    <Button variant="contained" sx={{textTransform: 'none'}}
                            onClick={() => navigate(`/stores/${storeId}/edit`)}>
                        Edit
                    </Button>
                )}
            </Box>
            <Box sx={{mx: 5}}>
                <StoreDetail store={store}/>
                <MenuList storeId={storeId}/>
            </Box>
        </Box>
    )
}

export default StoreDetailPage
