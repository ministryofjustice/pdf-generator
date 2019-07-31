.PHONY: all ecr-login gradle-build build tag test push clean-remote clean-local

aws_region := eu-west-2
image := hmpps/new-tech-pdfgenerator
gradle_builder_image := gradle:jdk8

# pdfgenerator_version should be passed from command line
all:
	$(MAKE) ecr-login
	$(MAKE) gradle-build
	$(MAKE) build
	$(MAKE) test
	$(MAKE) push
	$(MAKE) clean-remote
	$(MAKE) clean-local

gradle-build: build_dir = $(shell pwd)
# gradle build expects just the PATCH value. MAJOR abd MINOR are hardcoded as 0.1
gradle-build: build_version = $(shell echo ${pdfgenerator_version} | awk -F . '{print $$3}')
gradle-build:
	$(info Running gradle build task for patch version $(build_version) from tag ${pdfgenerator_version})
	# Build container runs as root - need to fix up perms at end so jenkins can clear up the workspace
	docker run --rm -v $(build_dir):/build -w /build -e CIRCLE_BUILD_NUM=${build_version} $(gradle_builder_image) bash -c "./gradlew build; chmod -R 0777 build/ .gradle/"

ecr-login:
	$(shell aws ecr get-login --no-include-email --region ${aws_region})
	aws --region $(aws_region) ecr describe-repositories --repository-names "$(image)" | jq -r .repositories[0].repositoryUri > ecr.repo

build: ecr_repo = $(shell cat ./ecr.repo)
build:
	$(info Build of repo $(ecr_repo))
	docker build -t $(ecr_repo) --build-arg PDFGENERATOR_VERSION=${pdfgenerator_version}  -f docker/Dockerfile.aws .

tag: ecr_repo = $(shell cat ./ecr.repo)
tag:
	$(info Tag repo $(ecr_repo) $(pdfgenerator_version))
	docker tag $(ecr_repo) $(ecr_repo):$(pdfgenerator_version)

test: ecr_repo = $(shell cat ./ecr.repo)
test:
	bash -c "GOSS_FILES_STRATEGY=cp GOSS_FILES_PATH="./docker/tests/" GOSS_SLEEP=5 dgoss run $(ecr_repo):latest"

push: ecr_repo = $(shell cat ./ecr.repo)
push:
	docker tag  ${ecr_repo} ${ecr_repo}:${pdfgenerator_version}
	docker push ${ecr_repo}:${pdfgenerator_version}

clean-remote: untagged_images = $(shell aws ecr list-images --region $(aws_region) --repository-name "$(image)" --filter "tagStatus=UNTAGGED" --query 'imageIds[*]' --output json)
clean-remote:
	if [ "${untagged_images}" != "[]" ]; then aws ecr batch-delete-image --region $(aws_region) --repository-name "$(image)" --image-ids '${untagged_images}' || true; fi

clean-local: ecr_repo = $(shell cat ./ecr.repo)
clean-local:
	-docker rmi ${ecr_repo}:latest
	-docker rmi ${ecr_repo}:${pdfgenerator_version}
	-rm -f ./ecr.repo
	-rm -f build/libs/pdfGenerator-*.jar