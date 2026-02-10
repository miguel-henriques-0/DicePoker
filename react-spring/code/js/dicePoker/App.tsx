import { createBrowserRouter, RouterProvider } from 'react-router';
import { Home } from './components/Home';
import { Login } from './components/Login';
import {AuthenticationProvider} from "./context/AuthenticationProvider";
import {Layout} from "./components/Layout";
import {CreateGame} from "./components/CreateGame";
import {Game} from "./components/Game";
import {Statistics} from "./components/Statistics";

const router = createBrowserRouter([
    {
        path: '/',
        element: <Layout />,
        children: [
            {
                path: '/',
                element: <Home />,
            },
            {
                path: '/login',
                element: <Login />,
            },
            {
                path: '/create-game',
                element: <CreateGame />
            },
            {
                path: '/game/:id',
                element: <Game />
            },
            {
                path: '/statistics',
                element: <Statistics/>
            }
        ],
    },
]);

export function App() {
    return (
        <AuthenticationProvider>
            <RouterProvider router={router} />
        </AuthenticationProvider>
    );
}