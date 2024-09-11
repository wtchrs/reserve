import {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {Avatar, Box, Button, Grid, Paper, Typography} from '@mui/material'
import userService from '../services/userService'
import {User} from '../type'
import UserDetailSkeleton from './UserDetailSkeleton'

function UserDetailPage() {
    const navigate = useNavigate()
    const {username} = useParams<{ username: string }>()
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

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString(undefined, {year: 'numeric', month: 'long', day: 'numeric'})
    }

    if (error) {
        throw error
    }

    if (!user) {
        return <UserDetailSkeleton/>
    }

    return (
        <Box sx={{mt: 4, mb: 4}}>
            <Paper
                elevation={3}
                sx={{
                    padding: 4,
                    maxWidth: 800,
                    margin: '0 auto',
                    textAlign: 'center',
                }}
            >
                <Avatar
                    alt={user.username}
                    // src="/static/images/avatar/1.jpg"
                    sx={{ width: 120, height: 120, margin: '0 auto', marginBottom: 2 }}
                />
                <Typography variant="h4" component="h1" gutterBottom>
                    {user.nickname}
                </Typography>
                <Typography variant="subtitle1" color="textSecondary" gutterBottom>
                    @{user.username}
                </Typography>
                <Typography variant="body1" sx={{marginBottom: 2}}>
                    {user.description || 'No description available.'}
                </Typography>
                <Typography variant="caption" display="block" color="textSecondary">
                    Joined on: {formatDate(user.signUpDate)}
                </Typography>

                <Grid container spacing={2} sx={{marginTop: 3}}>
                    <Grid item xs={6}>
                        <Button
                            variant="outlined"
                            fullWidth
                            onClick={() => navigate(-1)}
                            sx={{textTransform: 'none'}}
                        >
                            Go Back
                        </Button>
                    </Grid>
                    <Grid item xs={6}>
                        <Button
                            variant="contained"
                            fullWidth
                            sx={{textTransform: 'none'}}
                            onClick={() => alert('Edit functionality is not implemented yet.')}
                        >
                            Edit
                        </Button>
                    </Grid>
                </Grid>
            </Paper>
        </Box>
    )
}

export default UserDetailPage
