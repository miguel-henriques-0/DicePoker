// @ts-ignore
import styles from '../css/user.module.css';
import { Link } from 'react-router';
import {useAuthentication} from "../context/authentication";

export function User() {
    const [username, setUsername, userId, setUserId] = useAuthentication();

    function handler() {
        setUsername(undefined)
        setUserId(undefined)
    }
    if (username) {
        // Logged In State
        return (
            <p className={styles.userInfo}>
                {username}
                <button className={styles.logoutButton} onClick={handler}>logout</button>
            </p>
        );
    } else {
        // Logged Out State (Login Link)
        return <Link to="/login" className={styles.loginLink}>login</Link>;
    }
}