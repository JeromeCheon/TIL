//ts-webpack-app
const path = require('path'); // 절대 경로를 참조하기 위해 path를 불러온다

const HtmlWebpackPlugin = require('html-webpack-plugin'); // html을 다루기 위한 플러그인을 불러옴

module.exports = {
	entry: {
		// 번들 파일로 만들기 위한 시작 파일 설정
		// 생성될 번들 파일은 js 폴더 하위에 app.js라는 이름으로 생성될 것이며 이 파일은 ./src/App.jsx를 시작으로 번들링함
		'js/app': ['./src/App.js'],
	},
	output: {
		// 생성된 번들 파일은 ./dist/ 폴더에 생성됨. 없으면 먼저 dist 폴더 생성함
		path: path.resolve(__dirname, 'dist/'),
		// publicPath를 지정함으로써 HTML등 다른 파일에서 생성된 번들을 참조할 때, /를 기준으로 참조함
		publicPath: '/',
	},
	module: {
		rules: [
			{
				test: /\.(js|jsx)$/,
				use: ['babel-loader'],
				exclude: /node_modules/,
			},
		],
	},
	// jsx, js 리액트 파일은 babel을 이용해서 빌드함
	plugins: [
		new HtmlWebpackPlugin({
			template: './src/index.html',
			filename: 'index.html',
		}),
	],
};
