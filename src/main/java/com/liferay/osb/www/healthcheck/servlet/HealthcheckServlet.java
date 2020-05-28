/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.osb.www.healthcheck.servlet;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.util.ContentTypes;

import java.io.IOException;

import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.dm.ComponentDeclaration;
import org.apache.felix.dm.diagnostics.DependencyGraph;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;

/**
 * @author Allen Ziegenfus
 */
@Component(
	immediate = true,
	property = {
		"osgi.http.whiteboard.context.path=/",
		"osgi.http.whiteboard.servlet.pattern=/healthcheck/*"
	},
	service = Servlet.class
)
public class HealthcheckServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException, ServletException {

		_writeHealthcheckHTML(httpServletResponse);
	}

	private String _generateHTML(String message) {
		StringBuffer sb = new StringBuffer();

		sb.append("<html>");
		sb.append("<body>");
		sb.append("<h1>");
		sb.append(message);
		sb.append("</h1>");
		sb.append("</body>");
		sb.append("</html>");

		return new String(sb);
	}

	private void _writeHealthcheckHTML(
		HttpServletResponse httpServletResponse) {

		httpServletResponse.setCharacterEncoding(StringPool.UTF8);
		httpServletResponse.setContentType(ContentTypes.TEXT_HTML_UTF8);

		int status = HttpServletResponse.SC_OK;
		String dependencyGraphState = "No unregistered components found";

		DependencyGraph graph = DependencyGraph.getGraph(
			DependencyGraph.ComponentState.UNREGISTERED,
			DependencyGraph.DependencyState.REQUIRED_UNAVAILABLE);

		List<ComponentDeclaration> unregisteredComponents =
			graph.getAllComponents();

		if (!unregisteredComponents.isEmpty()) {
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			dependencyGraphState =
				unregisteredComponents.size() +
					" unregistered components found";

			_log.warn(dependencyGraphState);

			for (ComponentDeclaration componentDeclaration :
					unregisteredComponents) {

				BundleContext bundleContext =
					componentDeclaration.getBundleContext();

				if (bundleContext != null) {
					Bundle bundle = bundleContext.getBundle();

					if (bundle != null) {
						_log.warn(
							"Found unregistered component " +
								componentDeclaration.getName() +
									" in bundle: " + bundle.getSymbolicName());
					}
				}
			}
		}

		httpServletResponse.setStatus(status);

		try {
			ServletResponseUtil.write(
				httpServletResponse, _generateHTML(dependencyGraphState));
		}
		catch (Exception e) {
			_log.warn(e.getMessage(), e);

			httpServletResponse.setStatus(
				HttpServletResponse.SC_PRECONDITION_FAILED);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		HealthcheckServlet.class);

	private static final long serialVersionUID = 1L;

}