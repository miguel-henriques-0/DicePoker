# Start the project

make sure you're inside the js dir!

- npm run dev

If theres a process running on the default port:

> Mac OS
> 
> lsof -i :5173
> 
> kill -9 PID

(Didn't test it not sure if right)
> Windows
>
> netstat -ano | findstr :5173
>
> taskkill /PID <PID> /F


Also make sure if Docker is running there isn't any
container with some mapping to the vite port.
