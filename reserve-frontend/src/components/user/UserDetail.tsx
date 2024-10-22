import {Avatar, Box, Typography} from '@mui/material'
import type {User} from '@customTypes/domain'
import {formatDate} from '@utils/date.ts'
import UserDetailSkeleton from './UserDetailSkeleton'

type Props = { user?: User }

function UserDetail({user}: Props) {
    if (!user) {
        return <UserDetailSkeleton/>
    }

    return (
        <Box>
            <Avatar
                alt={user.username}
                // src="/static/images/avatar/1.jpg"
                sx={{width: 120, height: 120, margin: '0 auto', marginBottom: 2}}
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
        </Box>
    )
}

export default UserDetail
