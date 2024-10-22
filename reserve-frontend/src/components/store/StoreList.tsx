import {Box, Grid, Typography} from '@mui/material'
import {useNavigate} from 'react-router-dom'
import type {Store} from '../../../types/domain.d.ts'

type Props = {
    stores: Store[]
}

function StoreList({stores}: Props) {
    const navigate = useNavigate()

    const handleRouteStore = (storeId: bigint) => {
        navigate(`/stores/${storeId}`)
    }

    return (
        <Grid container spacing={2} columns={{xs: 4, sm: 8, md: 12}}>
            {stores.map((store, index) => (
                <Grid item key={index} xs={4}>
                    <Box
                        onClick={() => handleRouteStore(store.storeId)}
                        sx={{
                            p: 2,
                            height: '100%',
                            border: '1px solid gray',
                            borderRadius: '8px',
                            cursor: 'pointer',
                            ':hover': {
                                boxShadow: 2,
                            },
                        }}
                    >
                        <Typography noWrap variant="h6">{store.name}</Typography>
                        <Typography noWrap variant="caption">{store.registrant}</Typography>
                        <Typography noWrap>{store.description}</Typography>
                    </Box>
                </Grid>
            ))}
        </Grid>
    )
}

export default StoreList
