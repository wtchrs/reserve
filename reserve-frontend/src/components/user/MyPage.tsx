import {useEffect, useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {Box, Button, Grid} from '@mui/material'
import {useAuth} from '@hooks/useAuth.tsx'
import userService from '@services/userService'
import type {User} from '@customTypes/domain'
import UserDetail from './UserDetail'
import UserDeleteDialog from './UserDeleteDialog.tsx'

function MyPage() {
    const {auth} = useAuth()

    const navigate = useNavigate()
    const [user, setUser] = useState<User>()
    const [error, setError] = useState()

    // Delete user dialog
    const [dialog, setDialog] = useState(false)

    useEffect(() => {
        if (!auth?.user.username) throw new Response('Unauthorized', {status: 401})
        userService.getUser(auth.user.username)
            .then(res => setUser(res))
            .catch(err => {
                console.log('err', err)
                setError(err)
            })
    }, [auth])

    if (error) {
        throw error
    }

    return (
        <Box sx={{mb: 4}}>
            <Grid container spacing={2} sx={{marginBottom: 3, justifyContent: 'space-between'}}>
                <Grid item>
                    <Button variant="text" onClick={() => navigate(-1)} sx={{textTransform: 'none'}}>
                        {'< Go Back'}
                    </Button>
                </Grid>
                <Grid item>
                    <Button variant="contained" onClick={() => navigate('/users/edit')} sx={{marginRight: 1}}>
                        Edit
                    </Button>
                    <Button variant="contained" onClick={() => navigate('/users/password')} sx={{marginRight: 1}}>
                        Change password
                    </Button>
                    <Button variant="outlined" color="error" onClick={() => setDialog(true)}>
                        Delete account
                    </Button>
                </Grid>
            </Grid>
            <Box sx={{padding: 4, maxWidth: 800, margin: '0 auto', textAlign: 'center'}}>
                <UserDetail user={user}/>
            </Box>

            <UserDeleteDialog open={dialog} onClose={() => setDialog(false)}/>
        </Box>
    )
}

export default MyPage
