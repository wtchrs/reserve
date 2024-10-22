import {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {Box, Button} from '@mui/material'
import UserDetail from './UserDetail'
import type {User} from '@customTypes/domain'
import userService from '@services/userService'

function UserDetailPage() {
    const {username} = useParams<{ username: string }>()
    const navigate = useNavigate()
    const [user, setUser] = useState<User>()
    const [error, setError] = useState()

    useEffect(() => {
        if (!username) throw new Response('Resource Not Found', {status: 404})
        userService.getUser(username)
            .then(res => setUser(res))
            .catch(err => {
                console.log('err', err)
                setError(err)
            })
    }, [username])

    if (!username) {
        throw new Response('Resource Not Found', {status: 404})
    }

    if (error) {
        throw error
    }

    return (
        <Box sx={{mb: 4}}>
            <Box sx={{mb: 3, display: 'flex', flexDirection: 'row', justifyContent: 'space-between'}}>
                <Button variant="text" sx={{textTransform: 'none'}} onClick={() => navigate(-1)}>
                    {'< Go Back'}
                </Button>
                <Button variant="text" sx={{textTransform: 'none'}}
                        onClick={() => navigate(`/users/${username}/stores`)}>
                    {username}'s Stores
                </Button>
            </Box>
            <Box sx={{padding: 4, maxWidth: 800, margin: '0 auto', textAlign: 'center'}}>
                <UserDetail user={user}/>
            </Box>
        </Box>
    )
}

export default UserDetailPage
