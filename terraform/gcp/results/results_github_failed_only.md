### Terraform Scan Results:

```
Passed Checks: 53, Failed Checks: 22, Skipped Checks: 0
```

| Check ID    | Check Name                                                                          | Resource                                         | Guideline    | File                       |
|-------------|-------------------------------------------------------------------------------------|--------------------------------------------------|--------------|----------------------------|
| CKV_GCP_21  | Ensure Kubernetes Clusters are configured with Labels                               | google_container_cluster.juniper_cluster         | [Link](None) | /cluster.tf                |
| CKV_GCP_65  | Manage Kubernetes RBAC users with Google Groups for GKE                             | google_container_cluster.juniper_cluster         | [Link](None) | /cluster.tf                |
| CKV_GCP_69  | Ensure the GKE Metadata Server is Enabled                                           | google_container_cluster.juniper_cluster         | [Link](None) | /cluster.tf                |
| CKV2_GCP_18 | Ensure GCP network defines a firewall and does not use the default firewall         | google_compute_network.juniper_network           | [Link](None) | /network.tf                |

---
### Kubernetes Scan Results:

```
Passed Checks: 146, Failed Checks: 43, Skipped Checks: 0
```

| Check ID   | Check Name                                                                  | Resource                                           | Guideline    | File                            |
|------------|-----------------------------------------------------------------------------|----------------------------------------------------|--------------|---------------------------------|
| CKV_K8S_21 | The default namespace should not be used                                    | Service.default.participant-service                | [Link](None) | /k8s/participant-service.yml    |
| CKV_K8S_21 | The default namespace should not be used                                    | ConfigMap.default.portal-b2c-configmap             | [Link](None) | /k8s/b2c-config.yml             |
| CKV_K8S_21 | The default namespace should not be used                                    | Ingress.default.juniper-ingress                    | [Link](None) | /k8s/ingress.yml                |
| CKV_K8S_37 | Minimize the admission of containers with capabilities assigned             | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_31 | Ensure that the seccomp profile is set to docker/default or runtime/default | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_8  | Liveness Probe Should be Configured                                         | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_20 | Containers should not run with allowPrivilegeEscalation                     | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_15 | Image Pull Policy should be Always                                          | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_13 | Memory limits should be set                                                 | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_40 | Containers should run as a high UID to avoid host conflict                  | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_22 | Use read-only filesystem for containers where possible                      | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_9  | Readiness Probe Should be Configured                                        | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_28 | Minimize the admission of containers with the NET_RAW capability            | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_29 | Apply security context to your pods and containers                          | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_30 | Apply security context to your containers                                   | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_38 | Ensure that Service Account Tokens are only mounted where necessary         | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_21 | The default namespace should not be used                                    | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_23 | Minimize the admission of root containers                                   | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_43 | Image should use digest                                                     | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_11 | CPU limits should be set                                                    | Deployment.default.admin-deployment                | [Link](None) | /k8s/admin-deployment.yml       |
| CKV_K8S_21 | The default namespace should not be used                                    | ServiceAccount.default.juniper-app-ksa             | [Link](None) | /k8s/juniper-ksa-user.yml       |
| CKV_K8S_21 | The default namespace should not be used                                    | ConfigMap.default.d2p-site-configmap               | [Link](None) | /k8s/site-config.yml            |
| CKV_K8S_21 | The default namespace should not be used                                    | ConfigMap.default.d2p-oauth2-configmap             | [Link](None) | /k8s/oauth2-config.yml          |
| CKV_K8S_21 | The default namespace should not be used                                    | Service.default.admin-service                      | [Link](None) | /k8s/admin-service.yml          |
| CKV_K8S_37 | Minimize the admission of containers with capabilities assigned             | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_31 | Ensure that the seccomp profile is set to docker/default or runtime/default | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_8  | Liveness Probe Should be Configured                                         | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_20 | Containers should not run with allowPrivilegeEscalation                     | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_15 | Image Pull Policy should be Always                                          | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_13 | Memory limits should be set                                                 | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_40 | Containers should run as a high UID to avoid host conflict                  | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_22 | Use read-only filesystem for containers where possible                      | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_9  | Readiness Probe Should be Configured                                        | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_28 | Minimize the admission of containers with the NET_RAW capability            | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_29 | Apply security context to your pods and containers                          | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_30 | Apply security context to your containers                                   | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_38 | Ensure that Service Account Tokens are only mounted where necessary         | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_21 | The default namespace should not be used                                    | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_23 | Minimize the admission of root containers                                   | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_43 | Image should use digest                                                     | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV_K8S_11 | CPU limits should be set                                                    | Deployment.default.participant-deployment          | [Link](None) | /k8s/participant-deployment.yml |
| CKV2_K8S_6 | Minimize the admission of pods which lack an associated NetworkPolicy       | Pod.default.admin-deployment.app-admin             | [Link](None) | /k8s/admin-deployment.yml       |
| CKV2_K8S_6 | Minimize the admission of pods which lack an associated NetworkPolicy       | Pod.default.participant-deployment.app-participant | [Link](None) | /k8s/participant-deployment.yml |

---
