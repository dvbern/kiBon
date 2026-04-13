import {LogLevel} from '@utils/log';
import {Environment} from './IEnvironment';

export const environment: Environment = {
    production: false,
    test: false,
    hmr: false,
    logLevel: LogLevel.INFO,
    logModules: {}
};
