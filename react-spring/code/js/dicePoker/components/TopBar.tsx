// @ts-ignore
import styles from '../css/topbar.module.css';
import {useAuthentication} from "../context/authentication";
import {User} from "./User";

export function TopBar() {
    const [username] = useAuthentication();
    return (
        <nav className={styles.navbar}>
            <div>
                <p>Chelas Dice Poker</p>
            </div>
            <User />
        </nav>
    )
}