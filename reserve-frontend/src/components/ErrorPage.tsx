import {Box, Button, Typography} from '@mui/material'
import {isRouteErrorResponse, useNavigate, useRouteError} from 'react-router-dom'
import {isAxiosError} from 'axios'
import {useEffect, useState} from 'react'

function ErrorPage() {
    const error = useRouteError();
    const navigate = useNavigate()

    const [title, setTitle] = useState('Error')
    const [message, setMessage] = useState('An unexpected error occurred. Please try again later.')
    const [status, setStatus] = useState(0)

    useEffect(() => {
        if (isRouteErrorResponse(error) || isAxiosError(error) || error instanceof Response) {
            switch (error.status) {
                case 400:
                    setTitle('Bad Request')
                    setMessage('The request could not be understood by the server due to malformed syntax.')
                    setStatus(400)
                    break;
                case 401:
                    navigate('/sign-in')
                    break;
                case 403:
                    setTitle('Forbidden')
                    setMessage('You do not have permission to access this page.')
                    setStatus(403)
                    break;
                case 404:
                    setTitle('Not Found')
                    setMessage('The requested page could not be found.')
                    setStatus(404)
                    break;
                case 409:
                    setTitle('Conflict')
                    setMessage('A conflict occurred while processing the request.')
                    setStatus(409)
                    break;
                case 500:
                    setTitle('Internal Server Error')
                    setMessage('An unexpected error occurred. Please try again later.')
                    setStatus(500)
                    break;
                default:
                    setTitle('Error')
                    setMessage('An unexpected error occurred. Please try again later.')
                    setStatus(0)
            }
        }
    }, [error, navigate])

    return (
        <Box>
            <Button variant="text" onClick={() => navigate(-1)} sx={{textTransform: 'none'}}>
                {'< Go Back'}
            </Button>
            <Box textAlign="center" mt={5}>
                <Typography variant="h3" gutterBottom>
                    {status ? `${status} - ${title}` : 'Error'}
                </Typography>
                <Typography variant="body1" paragraph>
                    {message}
                </Typography>
                <Button variant="contained" onClick={() => navigate('/')}>
                    Go to Home
                </Button>
            </Box>
        </Box>
    )
}

export default ErrorPage

