import {Box, Button, TextField, Typography} from '@mui/material'
import {useNavigate} from 'react-router-dom'
import {FormEvent, useRef} from 'react'

function SearchPage() {
    const navigate = useNavigate()
    const ref = useRef<HTMLInputElement | null>(null)

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault()
        const username = ref.current?.value
        console.log('username', username)
        if (!username) return
        navigate(`/users/${username}`)
    }

    return (
        <Box sx={{mb: 4}}>
            <Button variant="text" onClick={() => navigate(-1)} sx={{textTransform: 'none', marginBottom: 2}}>
                {'< Go Back'}
            </Button>

            <Box sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                padding: 2,
            }}>
                <Box
                    component="form"
                    onSubmit={handleSubmit}
                    sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        width: '100%',
                        maxWidth: 400,
                        mb: 4,
                        gap: 2,
                    }}
                >
                    <Typography variant="h6" align="center">
                        Search for a User
                    </Typography>

                    <TextField
                        inputRef={ref}
                        margin="normal"
                        label="Enter username"
                        fullWidth
                        variant="outlined"
                    />

                    <Button variant="contained" type="submit" fullWidth>
                        Search
                    </Button>
                </Box>
            </Box>
        </Box>
    )
}

export default SearchPage
