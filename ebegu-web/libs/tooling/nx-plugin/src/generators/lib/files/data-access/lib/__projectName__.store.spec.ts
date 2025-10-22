import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { <%= classify(name) %>Store } from './<%= dasherize(projectName) %>.store';

describe('<%= classify(name) %>Store', () => {
  let store: <%= classify(name) %>Store;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        <%= classify(name) %>Store,
        provideHttpClient(),
      ],
    });
    store = TestBed.inject(<%= classify(name) %>Store);
  });

  it('is initializable', () => {
    expect(store).toBeTruthy();
  });

  it('has a initial remote data state', () => {
    expect(store.<%= camelize(name) %>()).toEqual({
      type: 'initial',
      data: undefined,
      error: undefined,
    });
  });
});