/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler,
    HttpInterceptor,
    HttpRequest,
    HttpResponse
} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {Observable} from 'rxjs';
import {catchError, filter, finalize} from 'rxjs/operators';
import {HttpPendingService} from '../../services/http-pending.service';

@Injectable()
export class HttpPendingInterceptor implements HttpInterceptor {
    private readonly httpPendingService = inject(HttpPendingService);

    public intercept(
        request: HttpRequest<unknown>,
        next: HttpHandler
    ): Observable<HttpEvent<unknown>> {
        this.httpPendingService.pending(request);
        return next.handle(request).pipe(
            catchError(err => {
                this.httpPendingService.resolve(request);
                throw err;
            }),
            filter(
                event =>
                    event instanceof HttpResponse ||
                    event instanceof HttpErrorResponse
            ),
            finalize(() => this.httpPendingService.resolve(request))
        );
    }
}
