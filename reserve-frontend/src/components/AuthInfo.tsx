import {Box, Button, Link} from '@mui/material'
import {useAuth} from '../hooks/useAuth'
import {useCallback} from 'react'

function AuthInfo() {
    const {auth, signOut} = useAuth()
    const onSignOut = useCallback(async () => await signOut(), [signOut])

    return (
        <Box sx={{display: 'flex', gap: 2, alignItems: 'center'}}>
            {auth ? (
                <>
                    <Link href={'/users/' + auth.user.username}>{auth.user.nickname}</Link>
                    <Button onClick={onSignOut} variant="outlined">Sign out</Button>
                </>
            ) : (
                <>
                    <Button href="/sign-up" variant="outlined">Sign up</Button>
                    <Button href="/sign-in" variant="contained">Sign in</Button>
                </>
            )}
        </Box>
    )
}

export default AuthInfo
