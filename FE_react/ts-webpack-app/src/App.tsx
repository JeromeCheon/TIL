import * as React from 'react';
import * as ReactDOM from 'react-dom';
import About from './About';
import Info from './Info';
import Nav from './Nav';
import {
	BrowserRouter as Router,
	Route,
	Switch,
	Redirect,
} from 'react-router-dom';
import { Provider } from 'react-redux';
import TaskList from './components/TaskList';
import store from './lib/redux';
interface Props {}

const App: React.FC<Props> = () => {
	return (
		<>
			<Router>
				<Nav />
				<Switch>
					<Route exact path='/about' component={About} />
					<Route exact path='/info' component={Info} />
					{/* 아래에는 404 방지로, 다른 url을 쳤을 때 루트로 redirect 시킨다 */}
					<Route component={() => <Redirect to='/' />} />
				</Switch>
			</Router>
			<Provider store={store}>
				<TaskList />
			</Provider>
		</>
	);
};
ReactDOM.render(<App />, document.getElementById('app'));
