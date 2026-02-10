import { Outlet } from 'react-router';
import {TopBar} from "./TopBar";

export function Layout() {
    return (
        <>
            <TopBar />
            <Outlet />
        </>
    );
}